package com.aslmk.recordingorchestratorservice.messaging.kafka;

import com.aslmk.recordingorchestratorservice.dto.RecordingEventType;
import com.aslmk.recordingorchestratorservice.dto.RecordingStatusEvent;
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

    @KafkaListener(topics = "${user.kafka.stream-lifecycle-topic}", groupId = "${user.kafka.group-id}")
    public void handleStreamLifecycle(@Payload String payload) {
        StreamLifecycleEvent event = deserialize(payload, StreamLifecycleEvent.class);

        if (!event.getEventType().equals(StreamLifecycleType.STREAM_STARTED)) {
            log.debug("Ignoring stream event: '{}'", StreamLifecycleType.STREAM_STARTED);
            return;
        }
        log.info("Processing event '{}': streamerId='{}', streamerUsername='{}'",
                event.getEventType(), event.getStreamerId(), event.getStreamerUsername());
        orchestrationService.processStreamEvent(event);
    }

    @KafkaListener(topics = "${user.kafka.recording-lifecycle-topic}", groupId = "${user.kafka.group-id}")
    public void handleRecordingLifecycle(@Payload String payload) {
        RecordingStatusEvent event = deserialize(payload, RecordingStatusEvent.class);

        if (!event.getEventType().equals(RecordingEventType.RECORDING_FINISHED)) {
            log.debug("Ignoring recording event: '{}'", RecordingEventType.RECORDING_FINISHED);
            return;
        }

        log.info("Processing '{}' event: streamerId='{}', filename='{}'",
                event.getEventType(), event.getStreamerId(), event.getFilename());
        orchestrationService.processRecordingEvent(event);
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
