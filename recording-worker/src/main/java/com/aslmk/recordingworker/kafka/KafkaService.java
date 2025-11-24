package com.aslmk.recordingworker.kafka;

import com.aslmk.common.dto.RecordCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    @Value("${user.kafka.producer.topic}")
    private String topic;

    private final KafkaTemplate<String, RecordCompletedEvent> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, RecordCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(RecordCompletedEvent request) {
        log.info("Publishing RecordCompletedEvent to Kafka topic='{}': streamer='{}', file='{}'",
                topic,
                request.getStreamerUsername(),
                request.getFileName());
        kafkaTemplate.send(topic, request);
    }
}