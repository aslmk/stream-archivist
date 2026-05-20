package com.aslmk.storageservice.client;

import com.aslmk.storageservice.dto.StreamSessionStatus;
import com.aslmk.storageservice.dto.StreamStatusDto;
import com.aslmk.storageservice.exception.RecordingOrchestratorClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class RecordingOrchestratorClientImpl implements RecordingOrchestratorClient {
    private final RestClient restClient;

    @Value("${user.orchestrator.base-url}")
    private String ORCHESTRATOR_BASE_URL;

    public RecordingOrchestratorClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void notifyUploadCompleted(UUID streamId) {
        try {
            String INTERNAL_STREAM_STATUS_ENDPOINT = String
                    .format("/internal/streams/%s/status", streamId);
            StreamStatusDto dto = new StreamStatusDto(StreamSessionStatus.UPLOAD_COMPLETED.name());

            restClient.patch()
                    .uri(ORCHESTRATOR_BASE_URL + INTERNAL_STREAM_STATUS_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Upload completion notification sent",
                    kv("streamId", streamId));
        } catch (RestClientException e) {
            throw new RecordingOrchestratorClientException(String.format(
                    "Failed to notify orchestrator about completed upload: streamId='%s'", streamId),
                    e);
        }
    }
}
