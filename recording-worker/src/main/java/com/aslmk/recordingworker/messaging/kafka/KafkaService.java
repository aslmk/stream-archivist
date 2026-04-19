package com.aslmk.recordingworker.messaging.kafka;

import com.aslmk.recordingworker.dto.RecordedPartEvent;
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

    @Value("${user.kafka.producer.recording-parts-topic}")
    private String RECORDING_PARTS_TOPIC;

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

    public void send(RecordedPartEvent event) {
        log.debug("Publishing '{}' event to Kafka topic='{}': filePart='{}', partIndex='{}'",
                event.getEventType(),
                RECORDING_PARTS_TOPIC,
                event.getFilePartName(),
                event.getPartIndex());

        String payload = serialize(event);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(RECORDING_PARTS_TOPIC, null, null, payload);

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