package com.aslmk.trackerservice.kafka;

import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.exception.KafkaEventSerializationException;
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

    @Value("${user.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(StreamLifecycleEvent event) {
        log.info("Sending '{}' event to topic '{}': streamerId='{}'",
                event.getEventType(), topic, event.getStreamerId());

        String payload = serialize(event);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, null, null, payload);

        kafkaTemplate.send(record);

        log.info("Successfully sent event '{}' to topic '{}': streamerId='{}'",
                event.getEventType(), topic, event.getStreamerId());
    }

    private String serialize(StreamLifecycleEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new KafkaEventSerializationException(
                    String.format("Failed to serialize event: %s", event.getClass().getSimpleName()), e);
        }
    }
}
