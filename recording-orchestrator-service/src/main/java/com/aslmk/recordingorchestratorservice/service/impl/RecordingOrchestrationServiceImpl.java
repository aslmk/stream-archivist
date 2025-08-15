package com.aslmk.recordingorchestratorservice.service.impl;

import com.aslmk.recordingorchestratorservice.dto.RecordingRequestDto;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class RecordingOrchestrationServiceImpl implements RecordingOrchestrationService {

    private final RestClient restClient;

    public RecordingOrchestrationServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void processRecordingRequest(RecordingRequestDto recordingRequestDto) {
        log.info("Processing recording request");
        restClient.post()
                .uri("http://localhost:8080/record")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "streamerUsername", recordingRequestDto.getStreamerUsername(),
                        "streamQuality", recordingRequestDto.getStreamQuality()
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
