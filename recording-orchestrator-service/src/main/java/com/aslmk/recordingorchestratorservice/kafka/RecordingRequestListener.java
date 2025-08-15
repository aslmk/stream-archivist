package com.aslmk.recordingorchestratorservice.kafka;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecordingRequestListener {

    private final RecordingOrchestrationService orchestrationService;

    public RecordingRequestListener(RecordingOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @KafkaListener(topics = "${user.kafka.topic}", groupId = "${user.kafka.group-id}")
    public void handleRecordingRequest(@Payload RecordingRequestDto request) {
        log.info("Received recording request: \n{}", request.toString());
        orchestrationService.processRecordingRequest(request);
    }
}
