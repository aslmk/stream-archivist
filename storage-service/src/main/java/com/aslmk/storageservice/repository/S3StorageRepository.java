package com.aslmk.storageservice.repository;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.MultipartUploadDto;
import com.aslmk.storageservice.dto.PreSignedUrl;
import com.aslmk.storageservice.dto.UploadPartsInfo;
import com.aslmk.storageservice.dto.UploadingResponseDto;
import com.aslmk.storageservice.exception.StorageException;
import com.aslmk.storageservice.service.UploadSessionService;
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
    private final UploadSessionService uploadSessionService;

    public S3StorageRepository(AmazonS3 amazonS3Client, MinioClient minioClient,
                               UploadSessionService uploadSessionService) {
        this.amazonS3Client = amazonS3Client;
        this.minioClient = minioClient;
        this.uploadSessionService = uploadSessionService;
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
    public UploadingResponseDto processUpload(MultipartUploadDto dto) {
        log.debug("Processing multipart upload: s3Path={}, parts={}",
                dto.getS3ObjectPath(), dto.getFileParts());

        try {
            String uploadId;
            Optional<UploadSessionEntity> session = uploadSessionService
                    .findByS3ObjectPath(dto.getS3ObjectPath());

            if (session.isPresent()) {
                uploadId = session.get().getUploadId();
            } else {
                uploadId = generateUploadId(dto.getS3ObjectPath());
                uploadSessionService.saveIfNotExists(dto.getS3ObjectPath(), uploadId);
            }

            UploadPartsInfo uploadParts = generateUploadUrls(uploadId, dto);

            log.debug("Generated {} presigned URLs for uploadId={}",
                    uploadParts.uploadUrls().size(), uploadId);

            return UploadingResponseDto.builder()
                    .uploadId(uploadId)
                    .uploadUrls(uploadParts.uploadUrls())
                    .hasNext(uploadParts.hasNext())
                    .nextPartNumberMarker(uploadParts.nextPartNumberMarker())
                    .build();

        } catch (Exception e) {
            log.error("Failed to process multipart upload for s3Path={}",
                    dto.getS3ObjectPath(), e);
            throw new StorageException("Could not process multipart upload: " + e.getMessage());
        }

    }

    private String generateUploadId(String objectKey) {
        log.debug("Requesting uploadId for key={}", objectKey);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(BUCKET_NAME, objectKey);
       InitiateMultipartUploadResult result = amazonS3Client.initiateMultipartUpload(request);
        log.debug("Received uploadId={} for key={}", result.getUploadId(), objectKey);
        return result.getUploadId();
    }

    private UploadPartsInfo generateUploadUrls(String uploadId, MultipartUploadDto dto) {
        log.debug("Generating {} presigned URLs for uploadId={} (key={})",
                dto.getFileParts(), uploadId, dto.getS3ObjectPath());


        PartListing uploadedPartsInfo = getUploadedPartsInfo(dto.getS3ObjectPath(),
                uploadId, dto.getNextPartNumberMarker());

        List<PreSignedUrl> uploadUrls = getMissingParts(uploadedPartsInfo.getParts(),
                dto.getFileParts(), uploadId, dto.getS3ObjectPath());

        if (uploadUrls.isEmpty()) {
            completeUpload(uploadedPartsInfo.getParts(), uploadId, dto.getS3ObjectPath());
            return new UploadPartsInfo(Collections.emptyList(), null, false);
        }

        return new UploadPartsInfo(uploadUrls,
                uploadedPartsInfo.getNextPartNumberMarker(),
                uploadedPartsInfo.isTruncated());
    }

    private URL generateUploadUrl(String uploadId, int partNumber, String objectKey) {
        GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(BUCKET_NAME, objectKey);

        presignedUrlRequest.setMethod(HttpMethod.PUT);
        presignedUrlRequest.addRequestParameter("uploadId", uploadId);
        presignedUrlRequest.addRequestParameter("partNumber", String.valueOf(partNumber));

        return amazonS3Client.generatePresignedUrl(presignedUrlRequest);
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
}
