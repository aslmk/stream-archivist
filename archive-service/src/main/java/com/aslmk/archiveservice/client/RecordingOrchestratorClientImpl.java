package com.aslmk.archiveservice.client;

import com.aslmk.archiveservice.dto.StreamListResponse;
import com.aslmk.archiveservice.exception.RecordingOrchestratorClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class RecordingOrchestratorClientImpl implements RecordingOrchestratorClient {
    @Value("${user.orchestrator.base-url}")
    private String ORCHESTRATOR_BASE_URL;

    private final RestClient restClient;

    public RecordingOrchestratorClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public StreamListResponse findStreamIdsByStreamerId(UUID streamerId) {
        try {
            String internalStreamersEndpoint = String.format("/internal/streamers/%s/streams", streamerId);
            return restClient.get()
                    .uri(ORCHESTRATOR_BASE_URL + internalStreamersEndpoint)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(StreamListResponse.class)
                    .getBody();
        } catch (RestClientException e) {
            throw new RecordingOrchestratorClientException("Failed to fetch stream references by streamerId=" +
                    streamerId,
                    e);
        }
    }
}
