package com.aslmk.storageservice.kafka;

import com.aslmk.storageservice.dto.UploadCompletedEvent;
import com.aslmk.storageservice.exception.KafkaEventDeserializationException;
import com.aslmk.storageservice.service.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UploadCompletedListener {

    private final StorageService service;
    private final ObjectMapper objectMapper;

    public UploadCompletedListener(StorageService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${user.kafka.topic}", groupId = "${user.kafka.group-id}")
    public void handleUploadCompletedEvent(@Payload String payload) {
        UploadCompletedEvent event = deserialize(payload, UploadCompletedEvent.class);

        log.info("Processing upload complete event: uploadId='{}', streamer='{}', filename='{}', parts='{}'",
                event.getUploadId(),
                event.getStreamerUsername(),
                event.getFilename(),
                event.getPartUploadResults().size()
        );

        service.completeUpload(event);

        log.info("Multipart upload completed successfully: uploadId={}", event.getUploadId());
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
