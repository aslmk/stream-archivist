package com.aslmk.recordingworker.kafka;

import com.aslmk.recordingworker.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.exception.KafkaEventSerializationException;
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

    public void send(RecordingStatusEvent event) {
        log.info("Publishing '{}' event to Kafka topic='{}': streamerId='{}', file='{}'",
                event.getEventType(),
                topic,
                event.getStreamerId(),
                event.getFilename());

        String payload = serialize(event);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, null, null, payload);

        kafkaTemplate.send(record);
    }

    private String serialize(RecordingStatusEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new KafkaEventSerializationException(
                    String.format("Failed to serialize event: %s", event.getClass().getSimpleName()), e);
        }
    }
}