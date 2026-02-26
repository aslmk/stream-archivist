package com.aslmk.storageservice.repository;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.aslmk.storageservice.dto.UploadingResponseDto;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;
import com.aslmk.storageservice.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class S3StorageRepository implements StorageRepository {

    @Value("${minio.bucketName}")
    private String bucketName;

    private final AmazonS3 amazonS3Client;
    private final MinioClient minioClient;

    public S3StorageRepository(AmazonS3 amazonS3Client, MinioClient minioClient) {
        this.amazonS3Client = amazonS3Client;
        this.minioClient = minioClient;
    }

    @PostConstruct
    private void initBucket() {
        try {
            log.debug("Checking if bucket '{}' exists", bucketName);

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                log.info("Bucket '{}' not found — creating...", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                log.debug("Bucket '{}' already exists", bucketName);
            }

        } catch (Exception e) {
            log.error("Failed to initialize bucket '{}'", bucketName, e);
            throw new StorageException("Could not create bucket: " + e.getMessage());
        }
    }

    @Override
    public UploadingResponseDto initiateUpload(InitMultipartUploadDto dto) {
        log.debug("Initiating multipart upload: s3Path={}, parts={}",
                dto.getS3ObjectPath(), dto.getFileParts());

        try {
            String uploadId = generateUploadId(dto.getS3ObjectPath());
            List<String> uploadUrls = generateUploadUrls(uploadId, dto);

            log.debug("Generated {} presigned URLs for uploadId={}",
                    uploadUrls.size(), uploadId);

            return UploadingResponseDto.builder()
                    .uploadId(uploadId)
                    .uploadURLs(uploadUrls)
                    .build();

        } catch (Exception e) {
            log.error("Failed to initiate multipart upload for s3Path={}",
                    dto.getS3ObjectPath(), e);
            throw new StorageException("Could not initiate multipart upload: " + e.getMessage());
        }

    }

    @Override
    public void completeUpload(CompleteMultipartUploadRequest request) {
        log.debug("Completing multipart upload: uploadId={}, key={}",
                request.getUploadId(), request.getKey());

        try {
            request.setBucketName(bucketName);
            amazonS3Client.completeMultipartUpload(request);
            log.info("Multipart upload completed: uploadId={}", request.getUploadId());
        } catch (Exception e) {
            log.error("Failed to complete multipart upload: uploadId={}",
                    request.getUploadId(), e);
            throw new StorageException("Failed to complete multipart upload: " + e.getMessage());
        }
    }

    private String generateUploadId(String objectKey) {
        log.debug("Requesting uploadId for key={}", objectKey);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
       InitiateMultipartUploadResult result = amazonS3Client.initiateMultipartUpload(request);
        log.debug("Received uploadId={} for key={}", result.getUploadId(), objectKey);
        return result.getUploadId();
    }

    private List<String> generateUploadUrls(String uploadId, InitMultipartUploadDto dto) {
        log.debug("Generating {} presigned URLs for uploadId={} (key={})",
                dto.getFileParts(), uploadId, dto.getS3ObjectPath());

        List<String> uploadUrls = new ArrayList<>();

        for (int i = 0; i < dto.getFileParts(); i++) {
            URL partUrl = generateUploadUrl(uploadId, i+1, dto.getS3ObjectPath());
            uploadUrls.add(partUrl.toString());
        }

        return uploadUrls;
    }

    private URL generateUploadUrl(String uploadId, int partNumber, String objectKey) {
        GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);

        presignedUrlRequest.setMethod(HttpMethod.PUT);
        presignedUrlRequest.addRequestParameter("uploadId", uploadId);
        presignedUrlRequest.addRequestParameter("partNumber", String.valueOf(partNumber));

        return amazonS3Client.generatePresignedUrl(presignedUrlRequest);
    }
}
