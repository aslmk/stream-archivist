package com.aslmk.trackerservice.kafka;

import com.aslmk.common.dto.StreamLifecycleEvent;
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

    private final KafkaTemplate<String, StreamLifecycleEvent> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, StreamLifecycleEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(StreamLifecycleEvent request) {
        log.info("Sending RecordingRequestDto to Kafka: topic='{}'", topic);
        log.debug("Request details: streamer='{}', url='{}'",
                request.getStreamerUsername(), request.getStreamUrl());

        ProducerRecord<String, StreamLifecycleEvent> record =
                new ProducerRecord<>(topic, null, null, request);

        kafkaTemplate.send(record);

        log.info("RecordingRequestDto successfully sent: streamer='{}', streamUrl='{}'",
                request.getStreamerUsername(),
                request.getStreamUrl());
    }
}
