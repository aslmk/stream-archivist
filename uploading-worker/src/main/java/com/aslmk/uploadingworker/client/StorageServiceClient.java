package com.aslmk.uploadingworker.client;

import com.aslmk.uploadingworker.dto.HttpRangeInputStream;
import com.aslmk.uploadingworker.dto.InitUploadingRequest;
import com.aslmk.uploadingworker.dto.InitUploadingResponse;
import com.aslmk.uploadingworker.dto.UploadPartsInfo;
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
            throw new StorageServiceException("Upload initialization failed: storage-service request error", e);
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
                    .format("Failed to get upload parts: uploadId='%s'", uploadId), e);
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
                throw new StorageServiceException("Part upload failed: missing ETag in response headers");
            }
        } catch (RestClientException e) {
            throw new StorageServiceException("Part upload failed: storage-service request error", e);
        }
    }
}
