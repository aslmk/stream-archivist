package com.aslmk.uploadingworker.kafka.producer;

import com.aslmk.common.dto.UploadCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    @Value("${user.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, UploadCompletedEvent> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, UploadCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(UploadCompletedEvent request) {
        log.info("Sending to topic: {}", topic);
        log.info("Uploading to S3 completed. Parts count: {}", request.getPartUploadResults().size());
        kafkaTemplate.send(topic, request);
    }
}