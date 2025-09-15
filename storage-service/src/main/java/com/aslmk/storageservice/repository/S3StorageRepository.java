package com.aslmk.storageservice.repository;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;
import com.aslmk.storageservice.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

        } catch (Exception e) {
            throw new StorageException("Could not create bucket: " + e.getMessage());
        }
    }

    @Override
    public UploadingResponseDto initiateUpload(InitMultipartUploadDto dto) {
        String uploadId = generateUploadId(dto.getS3ObjectPath());
        List<String> uploadUrls = generateUploadUrls(uploadId, dto);

        return UploadingResponseDto.builder()
                .uploadId(uploadId)
                .uploadURLs(uploadUrls)
                .build();
    }

    @Override
    public void completeUpload(CompleteMultipartUploadRequest request) {
        request.setBucketName(bucketName);
        amazonS3Client.completeMultipartUpload(request);
    }

    private String generateUploadId(String objectKey) {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
       InitiateMultipartUploadResult result = amazonS3Client.initiateMultipartUpload(request);
       return result.getUploadId();
    }

    private List<String> generateUploadUrls(String uploadId, InitMultipartUploadDto dto) {
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
