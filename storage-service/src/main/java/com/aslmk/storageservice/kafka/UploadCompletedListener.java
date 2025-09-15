package com.aslmk.storageservice.kafka;

import com.aslmk.common.dto.UploadCompletedEvent;
import com.aslmk.storageservice.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UploadCompletedListener {
    private final StorageService service;

    public UploadCompletedListener(StorageService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${user.kafka.topic}", groupId = "${user.kafka.group-id}")
    public void handleRecordCompletedEvent(@Payload UploadCompletedEvent uploadCompletedEvent) {
        log.info("Completing multipart upload for file: {}", uploadCompletedEvent.getFilename());
        service.completeUpload(uploadCompletedEvent);
    }
}
