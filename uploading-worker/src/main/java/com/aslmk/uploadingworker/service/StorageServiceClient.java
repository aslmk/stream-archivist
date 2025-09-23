package com.aslmk.uploadingworker.service;

import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.uploadingworker.dto.S3PartDto;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;

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
            throw new StorageServiceException("Upload initialization failed: storage service request error", e);
        }

        if (response == null) {
            throw new StorageServiceException("Upload initialization failed: response body is null");
        }

        return response;
    }

    public String uploadChunk(S3PartDto s3Part) {
        if (s3Part.getPartData().length == 0) {
            throw new StorageServiceException("Chunk upload failed: S3 part data is empty");
        }

        isValidUrl(s3Part.getPreSignedUrl());
        URI uri = URI.create(s3Part.getPreSignedUrl());

        ResponseEntity<?> response;
        try {
             response = restClient.put()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(s3Part.getPartData().length)
                    .body(s3Part.getPartData())
                    .retrieve()
                    .toEntity(ResponseEntity.class);
        } catch (Exception e) {
            throw new StorageServiceException("Chunk upload failed: storage service request error", e);
        }

        String etag = response.getHeaders().getFirst("ETag");

        if (etag == null || etag.isBlank()) {
            throw new StorageServiceException("Chunk upload failed: missing ETag in response headers");
        }

        return etag;
    }

    private void isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new StorageServiceException("URL is empty");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new StorageServiceException("Invalid URL: " + url, e);
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new StorageServiceException("URL is not http or https: " + url);
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new StorageServiceException("URL must have a valid host: " + url);
        }
    }
}
