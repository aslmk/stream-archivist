package com.aslmk.uploadingworker.client;

import com.aslmk.uploadingworker.dto.*;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;

@Slf4j
@Component
public class StorageServiceClient {

    @Value("${user.storage-service-url}")
    private String storageServiceUrl;

    public static final String INTERNAL_UPLOAD_INIT_ENDPOINT = "/internal/storage/uploads";
    public static final String INTERNAL_GET_UPLOAD_PARTS_ENDPOINT = "/internal/storage/uploads/%s/parts";
    public static final String INTERNAL_UPLOAD_COMPLETE_ENDPOINT = "/internal/storage/uploads/complete";

    private final RestClient restClient;

    public StorageServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public InitUploadingResponse initUpload(InitUploadingRequest request) {
        try {
            return restClient.post()
                    .uri(storageServiceUrl + INTERNAL_UPLOAD_INIT_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(InitUploadingResponse.class)
                    .getBody();
        } catch (RestClientException e) {
            throw new StorageServiceException(String
                    .format("Failed to init multipart upload: streamId='%s', filename='%s', error='%s'",
                            request.streamId(), request.fileName(), e.getMessage()));
        }
    }

    public UploadPartsInfo getUploadParts(String uploadId, Integer partNumberMarker) {
        try {
            String url = String.format(INTERNAL_GET_UPLOAD_PARTS_ENDPOINT, uploadId);
            return restClient.get()
                    .uri(storageServiceUrl + url + "?partNumberMarker=" + partNumberMarker)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(UploadPartsInfo.class)
                    .getBody();
        } catch (RestClientException e) {
            throw new StorageServiceException(String
                    .format("Failed to get upload parts: uploadId='%s', error='%s'",
                            uploadId, e.getMessage()));
        }
    }

    public void uploadPart(String url, HttpRangeInputStream hris, long partSize) {
        try {
            ResponseEntity<Void> response;
            URI plainUri = URI.create(url);

            response = restClient.put()
                    .uri(plainUri)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(partSize)
                    .body(new InputStreamResource(hris))
                    .retrieve()
                    .toBodilessEntity();

            String etag = response.getHeaders().getFirst("ETag");

            if (etag == null || etag.isBlank()) {
                throw new StorageServiceException("Failed to upload part: missing ETag in response headers");
            }
        } catch (RestClientException e) {
            throw new StorageServiceException("Failed to upload part: " + e.getMessage());
        }
    }

    public void compelteUpload(CompleteUploadingRequest request) {
        try {
            restClient.post()
                    .uri(storageServiceUrl + INTERNAL_UPLOAD_COMPLETE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new StorageServiceException(String
                    .format("Failed to complete multipart upload: streamId='%s', filename='%s', error='%s'",
                            request.streamId(), request.fileName(), e.getMessage()));
        }
    }
}
