package com.aslmk.trackerservice.kafka;

import com.aslmk.trackerservice.dto.RecordingRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    @Value("${user.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, RecordingRequestDto> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, RecordingRequestDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(RecordingRequestDto request) {
        log.info("Sending to topic {}", topic);
        log.info("Sending recordingRequest: {}", request.toString());
        kafkaTemplate.send(topic, request);
    }
}
