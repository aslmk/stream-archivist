package com.aslmk.uploadingworker.kafka.consumer;

import com.aslmk.common.dto.RecordCompletedEvent;
import com.aslmk.uploadingworker.service.StreamUploaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecordCompletedListener {

    private final StreamUploaderService service;

    public RecordCompletedListener(StreamUploaderService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${user.kafka.consumer.topic}", groupId = "${user.kafka.group-id}")
    public void handleRecordCompletedEvent(@Payload RecordCompletedEvent recordCompletedEvent) {
        log.info("Received 'record completed' message for streamer: {}", recordCompletedEvent.getStreamerUsername());
        service.processUploadingRequest(recordCompletedEvent);
    }
}
