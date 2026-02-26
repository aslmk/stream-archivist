package com.aslmk.uploadingworker.kafka.consumer;

import com.aslmk.uploadingworker.dto.RecordingEventType;
import com.aslmk.uploadingworker.dto.RecordingStatusEvent;
import com.aslmk.uploadingworker.exception.KafkaEventDeserializationException;
import com.aslmk.uploadingworker.service.StreamUploaderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecordCompletedListener {

    private final StreamUploaderService service;
    private final ObjectMapper objectMapper;

    public RecordCompletedListener(StreamUploaderService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${user.kafka.consumer.topic}", groupId = "${user.kafka.group-id}")
    public void handleRecordCompletedEvent(@Payload String payload) {
        RecordingStatusEvent event = deserialize(payload, RecordingStatusEvent.class);

        if (!event.getEventType().equals(RecordingEventType.RECORDING_FINISHED)) {
            log.debug("Ignoring stream event: {}", event.getEventType());
            return;
        }

        log.info("Processing '{}' event: streamerId='{}', filename='{}'",
                event.getEventType(), event.getStreamerId(), event.getFilename());

        service.processUploadingRequest(event);

        log.info("Uploading successfully processed: streamerId='{}', filename='{}'",
                event.getStreamerUsername(), event.getFilename());
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
