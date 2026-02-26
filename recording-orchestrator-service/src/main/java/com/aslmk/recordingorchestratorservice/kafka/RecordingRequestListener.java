package com.aslmk.recordingorchestratorservice.kafka;

import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;
import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleType;
import com.aslmk.recordingorchestratorservice.exception.KafkaEventDeserializationException;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecordingRequestListener {

    private final RecordingOrchestrationService orchestrationService;
    private final ObjectMapper objectMapper;

    public RecordingRequestListener(RecordingOrchestrationService orchestrationService,
                                    ObjectMapper objectMapper) {
        this.orchestrationService = orchestrationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${user.kafka.topic}", groupId = "${user.kafka.group-id}")
    public void handleRecordingRequest(@Payload String payload) {
        StreamLifecycleEvent event = deserialize(payload, StreamLifecycleEvent.class);

        if (!event.getEventType().equals(StreamLifecycleType.STREAM_STARTED)) {
            log.debug("Ignoring stream event: {}", event.getEventType());
            return;
        }
        log.info("Processing event '{}': streamerId='{}'", event.getEventType(), event.getStreamerId());
        orchestrationService.processRecordingRequest(event);
    }

    private <T> T deserialize(String data, Class<T> c) {
        try {
            return objectMapper.readValue(data, c);
        } catch (JsonProcessingException e) {
            throw new KafkaEventDeserializationException(
                    String.format("Failed to deserialize JSON as instance of class '%s'", c.getSimpleName()), e);
        }
    }
}
