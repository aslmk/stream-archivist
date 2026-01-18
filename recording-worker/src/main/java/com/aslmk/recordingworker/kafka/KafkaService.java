package com.aslmk.recordingworker.kafka;

import com.aslmk.common.dto.RecordingStatusEvent;
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

    private final KafkaTemplate<String, RecordingStatusEvent> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, RecordingStatusEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(RecordingStatusEvent request) {
        log.info("Publishing '{}' event to Kafka topic='{}': streamer='{}', file='{}'",
                request.getEventType(),
                topic,
                request.getStreamerUsername(),
                request.getFilename());

        ProducerRecord<String, RecordingStatusEvent> record =
                new ProducerRecord<>(topic, null, null, request);

        kafkaTemplate.send(record);
    }
}