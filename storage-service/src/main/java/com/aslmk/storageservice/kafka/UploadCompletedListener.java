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
    public void handleRecordCompletedEvent(@Payload UploadCompletedEvent event) {
        log.info("Received UploadCompletedEvent: uploadId={}, streamer={}, filename={}, parts={}",
                event.getUploadId(),
                event.getStreamerUsername(),
                event.getFilename(),
                event.getPartUploadResults().size()
        );

        service.completeUpload(event);
        log.info("Multipart upload completed successfully: uploadId={}", event.getUploadId());
    }
}
