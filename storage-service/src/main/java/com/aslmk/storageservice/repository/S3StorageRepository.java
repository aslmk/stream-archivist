package com.aslmk.storageservice.repository;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.aslmk.storageservice.dto.PreSignedUrl;
import com.aslmk.storageservice.dto.UploadPartsInfo;
import com.aslmk.storageservice.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.util.*;

@Slf4j
@Repository
public class S3StorageRepository implements StorageRepository {

    @Value("${minio.bucketName}")
    private String BUCKET_NAME;

    @Value("${user.storage.batch.max-size}")
    private int BATCH_MAX_SIZE;

    private final AmazonS3 amazonS3Client;
    private final MinioClient minioClient;

    public S3StorageRepository(AmazonS3 amazonS3Client, MinioClient minioClient) {
        this.amazonS3Client = amazonS3Client;
        this.minioClient = minioClient;
    }

    @PostConstruct
    private void initBucket() {
        try {
            log.debug("Checking if bucket '{}' exists", BUCKET_NAME);

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());

            if (!found) {
                log.info("Bucket '{}' not found — creating...", BUCKET_NAME);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            } else {
                log.debug("Bucket '{}' already exists", BUCKET_NAME);
            }

        } catch (Exception e) {
            log.error("Failed to initialize bucket '{}'", BUCKET_NAME, e);
            throw new StorageException("Could not create bucket: " + e.getMessage());
        }
    }

    @Override
    public UploadPartsInfo getUploadPart(String uploadId, String objectKey,
                              Integer partNumberMarker, int expectedParts) {
        PartListing uploadedParts = getUploadedPartsInfo(objectKey, uploadId, partNumberMarker);

        List<PreSignedUrl> missingParts = getMissingParts(uploadedParts.getParts(),
                expectedParts, uploadId, objectKey);

        if (missingParts.isEmpty()) {
            completeUpload(uploadedParts.getParts(), uploadId, objectKey);
            return new UploadPartsInfo(Collections.emptyList(), null, false);
        }

        return new UploadPartsInfo(missingParts,
                uploadedParts.getNextPartNumberMarker(),
                uploadedParts.isTruncated());
    }

    @Override
    public String generateUploadId(String objectKey) {
        log.debug("Requesting uploadId for key='{}'", objectKey);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(BUCKET_NAME, objectKey);
        InitiateMultipartUploadResult result = amazonS3Client.initiateMultipartUpload(request);
        return result.getUploadId();
    }

    @Override
    @Deprecated(forRemoval = true)
    public PreSignedUrl generatePreSignedUrl(String uploadId, Long partNumber, String objectKey) {
        URL partUrl = generateUploadUrl(uploadId, partNumber, objectKey);
        return new PreSignedUrl(partNumber.intValue(), partUrl.toString());
    }

    @Override
    @Deprecated(forRemoval = true)
    public void completeChunkedUpload(String uploadId, String key) {
        List<PartETag> partETags = new ArrayList<>();

        PartListing uploadedPartsInfo = getUploadedPartsInfo(key, uploadId, 0);

        while (true) {
            List<PartSummary> uploadedParts = uploadedPartsInfo.getParts();
            for (PartSummary uploadedPart : uploadedParts) {
                partETags.add(new PartETag(uploadedPart.getPartNumber(), uploadedPart.getETag()));
            }

            if (!uploadedPartsInfo.isTruncated()) {
                break;
            }

            uploadedPartsInfo = getUploadedPartsInfo(key, uploadId,
                    uploadedPartsInfo.getNextPartNumberMarker());
        }

        completeChunkedUpload(partETags, uploadId, key);
    }

    private URL generateUploadUrl(String uploadId, long partNumber, String objectKey) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(BUCKET_NAME, objectKey);

        request.setMethod(HttpMethod.PUT);
        request.addRequestParameter("uploadId", uploadId);
        request.addRequestParameter("partNumber", String.valueOf(partNumber));

        return amazonS3Client.generatePresignedUrl(request);
    }

    private PartListing getUploadedPartsInfo(String key, String uploadId, Integer partNumberMarker) {
        ListPartsRequest request = new ListPartsRequest(BUCKET_NAME, key, uploadId)
                .withPartNumberMarker(partNumberMarker)
                .withMaxParts(BATCH_MAX_SIZE);

        return amazonS3Client.listParts(request);
    }

    private List<PreSignedUrl> getMissingParts(List<PartSummary> uploadedParts,
                                               int fileParts, String uploadId,
                                               String s3ObjectPath) {
        Set<Integer> missingParts = new HashSet<>();
        List<PreSignedUrl> urls = new ArrayList<>();

        int prev = 0;
        for (PartSummary uploadedPart : uploadedParts) {
            for (int i = prev+1; i < uploadedPart.getPartNumber(); i++) {
                missingParts.add(i);
            }
            prev = uploadedPart.getPartNumber();
        }

        for (int i = prev+1; i <= fileParts; i++) {
            missingParts.add(i);
        }

        for (int missingPart : missingParts) {
            URL partUrl = generateUploadUrl(uploadId, missingPart, s3ObjectPath);
            urls.add(new PreSignedUrl(missingPart, partUrl.toString()));
        }

        return urls;
    }

    private void completeUpload(List<PartSummary> uploadedParts, String uploadId, String key) {
        List<PartETag> partETags = new ArrayList<>();
        for (PartSummary uploadedPart : uploadedParts) {
            partETags.add(new PartETag(uploadedPart.getPartNumber(), uploadedPart.getETag()));
        }

        try {
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest();
            request.setUploadId(uploadId);
            request.setKey(key);
            request.setPartETags(partETags);
            request.setBucketName(BUCKET_NAME);
            amazonS3Client.completeMultipartUpload(request);
            log.info("Multipart upload completed: uploadId={}", request.getUploadId());
        } catch (Exception e) {
            throw new StorageException("Failed to complete multipart upload: " + e.getMessage());
        }
    }

    @Deprecated(forRemoval = true)
    private void completeChunkedUpload(List<PartETag> partETags, String uploadId, String key) {
        try {
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest();
            request.setUploadId(uploadId);
            request.setKey(key);
            request.setPartETags(partETags);
            request.setBucketName(BUCKET_NAME);
            amazonS3Client.completeMultipartUpload(request);
        } catch (Exception e) {
            throw new StorageException("Failed to complete multipart upload: " + e.getMessage());
        }
    }
}
