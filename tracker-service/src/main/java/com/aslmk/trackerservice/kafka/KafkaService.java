package com.aslmk.trackerservice.kafka;

import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.exception.KafkaEventSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<SendResult<String, String>> send(StreamLifecycleEvent event) {
        String payload = serialize(event);
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, null, null, payload);

        return kafkaTemplate.send(record);
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
