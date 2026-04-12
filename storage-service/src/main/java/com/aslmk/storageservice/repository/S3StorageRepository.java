package com.aslmk.storageservice.repository;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;
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
    private String bucketName;

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
                    .uploadURLs(uploadParts.uploadUrls())
                    .hasNext(uploadParts.hasNext())
                    .nextPartNumberMarker(uploadParts.nextPartNumberMarker())
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

    private UploadPartsInfo generateUploadUrls(String uploadId, InitMultipartUploadDto dto) {
        log.debug("Generating {} presigned URLs for uploadId={} (key={})",
                dto.getFileParts(), uploadId, dto.getS3ObjectPath());


        PartListing uploadedPartsInfo = getUploadedPartsInfo(dto.getS3ObjectPath(),
                uploadId, dto.getNextPartNumberMarker());

        Map<Integer, String> uploadUrls = getMissingParts(uploadedPartsInfo.getParts(),
                dto.getFileParts(), uploadId, dto.getS3ObjectPath());

        return new UploadPartsInfo(uploadUrls,
                uploadedPartsInfo.getNextPartNumberMarker(),
                uploadedPartsInfo.isTruncated());
    }

    private URL generateUploadUrl(String uploadId, int partNumber, String objectKey) {
        GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);

        presignedUrlRequest.setMethod(HttpMethod.PUT);
        presignedUrlRequest.addRequestParameter("uploadId", uploadId);
        presignedUrlRequest.addRequestParameter("partNumber", String.valueOf(partNumber));

        return amazonS3Client.generatePresignedUrl(presignedUrlRequest);
    }

    private PartListing getUploadedPartsInfo(String key, String uploadId, Integer partNumberMarker) {
        ListPartsRequest request = new ListPartsRequest(bucketName, key, uploadId)
                .withPartNumberMarker(partNumberMarker)
                .withMaxParts(100); // TODO: move max-parts to the application.properties
                                    // TODO: IMPORTANT! max-parts should be < 1000.

        return amazonS3Client.listParts(request);
    }

    private Map<Integer, String> getMissingParts(List<PartSummary> uploadedParts,
                                                 int fileParts, String uploadId,
                                                 String s3ObjectPath) {
        Map<Integer, String> uploadUrls = new HashMap<>();
        Set<Integer> missingParts = new HashSet<>();

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
            uploadUrls.put(missingPart, partUrl.toString());
        }

        return uploadUrls;
    }
}
