package com.aslmk.recordingorchestratorservice.kafka;

import com.aslmk.common.constants.StreamLifecycleType;
import com.aslmk.common.dto.StreamLifecycleEvent;
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
    public void handleRecordingRequest(@Payload StreamLifecycleEvent request) {
        if (!request.getEventType().equals(StreamLifecycleType.STREAM_STARTED)) {
            log.debug("Ignoring stream event: {}", request.getEventType());
            return;
        }

        log.info("Received recording request: streamerUsername={}, streamUrl={}",
                request.getStreamerUsername(),
                request.getStreamUrl());
        orchestrationService.processRecordingRequest(request);
    }
}
