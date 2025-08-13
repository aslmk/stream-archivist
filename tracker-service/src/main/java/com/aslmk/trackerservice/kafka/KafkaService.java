package com.aslmk.trackerservice.kafka;

import com.aslmk.trackerservice.dto.StreamerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    @Value("${user.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, StreamerDto> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, StreamerDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(StreamerDto streamer) {
        log.info("Sending to topic {}", topic);
        log.info("Sending streamer: {}", streamer.toString());
        kafkaTemplate.send(topic, streamer);
    }
}
