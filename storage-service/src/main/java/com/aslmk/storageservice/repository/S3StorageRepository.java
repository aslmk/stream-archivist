package com.aslmk.storageservice.repository;


import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Repository
public class S3StorageRepository implements StorageRepository {

    @Value("${minio.bucketName}")
    private String BUCKET_NAME;

    @Value("${user.storage.batch.max-size}")
    private int BATCH_MAX_SIZE;

    private final AmazonS3 amazonS3Client;
    private final AmazonS3 presignS3Client;
    private final MinioClient minioClient;

    public S3StorageRepository(AmazonS3 amazonS3Client,
                               @Qualifier("presignS3Client") AmazonS3 presignS3Client,
                               MinioClient minioClient) {
        this.amazonS3Client = amazonS3Client;
        this.presignS3Client = presignS3Client;
        this.minioClient = minioClient;
    }

    @PostConstruct
    private void initBucket() {
        try {
            log.debug("Checking if bucket exists", kv("bucketName", BUCKET_NAME));

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());

            if (!found) {
                log.debug("Bucket not found. Creating", kv("bucketName", BUCKET_NAME));
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            } else {
                log.debug("Bucket is already exists", kv("bucketName", BUCKET_NAME));
            }

        } catch (Exception e) {
            log.error("Failed to initialize bucket",
                    kv("bucketName", BUCKET_NAME),
                    e);
            throw new StorageException(String
                    .format("Could not create '%s' bucket: %s", BUCKET_NAME, e.getMessage()));
        }
    }

    @Override
    public UploadPartsInfo getUploadPart(String uploadId, String objectKey,
                              Integer partNumberMarker, int expectedParts) {
        PartListing uploadedParts = getUploadedPartsInfo(objectKey, uploadId, partNumberMarker);

        List<PreSignedUrl> missingParts = getMissingParts(uploadedParts.getParts(),
                expectedParts, uploadId, objectKey);

        if (missingParts.isEmpty()) {
            return new UploadPartsInfo(Collections.emptyList(), null, false);
        }

        return new UploadPartsInfo(missingParts,
                uploadedParts.getNextPartNumberMarker(),
                uploadedParts.isTruncated());
    }

    @Override
    public String generateUploadId(String objectKey) {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(BUCKET_NAME, objectKey);
        InitiateMultipartUploadResult result = amazonS3Client.initiateMultipartUpload(request);
        return result.getUploadId();
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

    @Override
    public void completeUpload(String uploadId, String objectKey, int expectedParts) {
        int partNumberMarker = 0;
        List<PartETag> partETags = new ArrayList<>();
        boolean hasNext;
        do {
            PartListing uploadedParts = getUploadedPartsInfo(objectKey, uploadId, partNumberMarker);
            for (PartSummary uploadedPart : uploadedParts.getParts()) {
                partETags.add(new PartETag(uploadedPart.getPartNumber(), uploadedPart.getETag()));
            }
            hasNext = uploadedParts.isTruncated();
            partNumberMarker = uploadedParts.getNextPartNumberMarker();
        } while (hasNext);

        if (partETags.size() != expectedParts) {
            throw new StorageException(String.format(
                    "Uploaded and expected part counts do not match! uploadedParts='%d', expectedParts='%d'",
                    partETags.size(), expectedParts));
        }

        completeUpload(partETags, uploadId, objectKey);
    }

    private void completeUpload(List<PartETag> partETags, String uploadId, String key) {
        try {
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest();
            request.setUploadId(uploadId);
            request.setKey(key);
            request.setPartETags(partETags);
            request.setBucketName(BUCKET_NAME);
            amazonS3Client.completeMultipartUpload(request);
        } catch (SdkClientException e) {
            throw new StorageException("Failed to complete multipart upload: " + e.getMessage());
        }
    }

    @Override
    public String generateDownloadUrl(String objectKey) {
        Date expiration = Date.from(Instant.now().plus(Duration.ofHours(12)));
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(BUCKET_NAME,
                objectKey, HttpMethod.GET)
                .withExpiration(expiration);

        return presignS3Client.generatePresignedUrl(req).toString();
    }
}
