package com.aslmk.uploadingworker.client;

import com.aslmk.uploadingworker.dto.InitUploadingRequest;
import com.aslmk.uploadingworker.dto.InitUploadingResponse;
import com.aslmk.uploadingworker.dto.S3Part;
import com.aslmk.uploadingworker.dto.UploadPartsInfo;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URISyntaxException;

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

    public void uploadPart(S3Part s3Part) {
        if (s3Part.data().length == 0) {
            throw new StorageServiceException("Part upload failed: S3 part data is empty");
        }

        isValidUrl(s3Part.url());
        URI uri = URI.create(s3Part.url());

        ResponseEntity<?> response;
        try {
            response = restClient.put()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(s3Part.data().length)
                    .body(s3Part.data())
                    .retrieve()
                    .toEntity(ResponseEntity.class);
        } catch (RestClientException e) {
            throw new StorageServiceException("Part upload failed: storage-service request error", e);
        }

        String etag = response.getHeaders().getFirst("ETag");

        if (etag == null || etag.isBlank()) {
            throw new StorageServiceException("Part upload failed: missing ETag in response headers");
        }

        log.debug("Part uploaded successfully: etag='{}'", etag);
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
