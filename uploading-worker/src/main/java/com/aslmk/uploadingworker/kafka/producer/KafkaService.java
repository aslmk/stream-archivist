package com.aslmk.uploadingworker.kafka.producer;

import com.aslmk.uploadingworker.dto.UploadCompletedEvent;
import com.aslmk.uploadingworker.exception.KafkaEventSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    @Value("${user.kafka.producer.topic}")
    private String topic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(UploadCompletedEvent event) {
        log.info("Sending upload complete event to topic '{}': streamer='{}', uploadId='{}', parts='{}'",
                topic, event.getStreamerUsername(),
                event.getUploadId(), event.getPartUploadResults());

        String payload = serialize(event);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, null, null, payload);

        kafkaTemplate.send(record);

        log.info("Successfully sent upload complete event to topic '{}': streamerId='{}', uploadId='{}'",
                topic, event.getStreamerUsername(), event.getUploadId());
    }

    private String serialize(UploadCompletedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new KafkaEventSerializationException(
                    String.format("Failed to serialize event: %s", event.getClass().getSimpleName()), e);
        }
    }
}