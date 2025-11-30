package com.aslmk.uploadingworker.kafka.producer;

import com.aslmk.common.dto.UploadCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    @Value("${user.kafka.producer.topic}")
    private String topic;

    private final KafkaTemplate<String, UploadCompletedEvent> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, UploadCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(UploadCompletedEvent request) {
        log.info("Sending UploadCompletedEvent to Kafka: topic='{}'", topic);
        log.debug("Event details: streamer='{}', filename='{}', parts={}, uploadId='{}'",
                request.getStreamerUsername(), request.getFilename(),
                request.getPartUploadResults().size(), request.getUploadId());

        kafkaTemplate.send(topic, request);

        log.info("UploadCompletedEvent successfully sent: filename='{}', streamer='{}'",
                request.getFilename(),
                request.getStreamerUsername());
    }
}