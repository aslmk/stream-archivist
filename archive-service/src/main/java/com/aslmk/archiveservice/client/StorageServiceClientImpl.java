package com.aslmk.archiveservice.client;

import com.aslmk.archiveservice.dto.RecordingDownloadRequest;
import com.aslmk.archiveservice.dto.RecordingDownloadsResponse;
import com.aslmk.archiveservice.exception.StorageServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class StorageServiceClientImpl implements StorageServiceClient {
    @Value("${user.storage.base-url}")
    private String STORAGE_BASE_URL;

    private final RestClient restClient;

    public StorageServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public RecordingDownloadsResponse getRecordingDownloads(UUID streamerId,
                                                            RecordingDownloadRequest request) {
        try {
            String internalRecordingsEndpoint = "/internal/recordings/downloads";
            return restClient.post()
                    .uri(STORAGE_BASE_URL + internalRecordingsEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(RecordingDownloadsResponse.class)
                    .getBody();
        } catch (RestClientException e) {
            throw new StorageServiceException("Failed to fetch recordings for streamerId=" +
                    streamerId,
                    e);
        }
    }
}
