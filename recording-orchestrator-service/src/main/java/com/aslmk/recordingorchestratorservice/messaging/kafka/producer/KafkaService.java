package com.aslmk.recordingorchestratorservice.messaging.kafka.producer;

import com.aslmk.recordingorchestratorservice.dto.RecordingStatusUpdatedEvent;
import com.aslmk.recordingorchestratorservice.exception.KafkaEventSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    @Value("${user.kafka.producer.recording-status-updated-topic}")
    private String RECORDING_STATUS_UPDATED_TOPIC;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate,
                        ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishRecordingUpdatedEvent(RecordingStatusUpdatedEvent event) {
        String payload = serialize(event);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(RECORDING_STATUS_UPDATED_TOPIC, null, null, payload);

        kafkaTemplate.send(record);
    }

    private <T> String serialize(T data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new KafkaEventSerializationException(
                    String.format("Failed to serialize event: %s", data.getClass().getSimpleName()), e);
        }
    }
}
