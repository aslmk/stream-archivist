package com.aslmk.uploadingworker.service;

import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.uploadingworker.dto.S3PartDto;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public class StorageServiceClient {

    @Value("${user.api.storage-service-base-url}")
    private String storageServiceBaseUrl;
    @Value("${user.api.storage-service-upload-init-endpoint}")
    private String uploadInitEndpoint;

    private final RestClient restClient;

    public StorageServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public UploadingResponseDto uploadInit(UploadingRequestDto request) {

        log.info("Starting uploadInit request to storage-service: streamer='{}', filename='{}', parts={}",
                request.getStreamerUsername(), request.getFileName(), request.getFileParts());

        UploadingResponseDto response;

        try {
            response = restClient.post()
                    .uri(storageServiceBaseUrl + uploadInitEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(UploadingResponseDto.class)
                    .getBody();
        } catch (Exception e) {
            log.error("uploadInit request failed", e);
            throw new StorageServiceException("Upload initialization failed: storage service request error", e);
        }

        if (response == null) {
            log.error("uploadInit response is null");
            throw new StorageServiceException("Upload initialization failed: response body is null");
        }

        log.info("uploadInit successful: uploadId='{}', uploadUrls={}",
                response.getUploadId(), response.getUploadURLs().size());

        return response;
    }

    public String uploadChunk(S3PartDto s3Part) {
        if (s3Part.getPartData().length == 0) {
            log.error("Chunk upload failed: empty part data");
            throw new StorageServiceException("Chunk upload failed: S3 part data is empty");
        }

        isValidUrl(s3Part.getPreSignedUrl());
        URI uri = URI.create(s3Part.getPreSignedUrl());

        ResponseEntity<?> response;
        try {
            log.debug("Sending PUT request to presigned URL");

            response = restClient.put()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(s3Part.getPartData().length)
                    .body(s3Part.getPartData())
                    .retrieve()
                    .toEntity(ResponseEntity.class);
        } catch (Exception e) {
            log.error("Chunk upload failed", e);
            throw new StorageServiceException("Chunk upload failed: storage service request error", e);
        }

        String etag = response.getHeaders().getFirst("ETag");

        if (etag == null || etag.isBlank()) {
            log.error("Missing ETag");
            throw new StorageServiceException("Chunk upload failed: missing ETag in response headers");
        }

        log.info("Chunk uploaded successfully, ETag={}", etag);

        return etag;
    }

    private void isValidUrl(String url) {
        log.debug("Validating presigned URL");

        if (url == null || url.isBlank()) {
            log.error("URL validation failed: empty URL");
            throw new StorageServiceException("URL is empty");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            log.error("Invalid URL syntax: {}", url);
            throw new StorageServiceException("Invalid URL: " + url, e);
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            log.error("Invalid URL scheme '{}': {}", scheme, url);
            throw new StorageServiceException("URL is not http or https: " + url);
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            log.error("URL host is missing: {}", url);
            throw new StorageServiceException("URL must have a valid host: " + url);
        }
        log.debug("URL validation passed");
    }
}
