package com.aslmk.recordingorchestratorservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orchestrator")
public class OrchestratorController {

    private final RestClient restClient;

    public OrchestratorController(RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping("/send-recording-request")
    public ResponseEntity<Void> sendRecordingRequest(@RequestParam String streamerUsername,
                                                       @RequestParam String streamQuality) {

        restClient.post()
                .uri("http://localhost:8080/record")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "streamerUsername", streamerUsername,
                        "streamQuality", streamQuality
                ))
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.noContent().build();
    }
}
