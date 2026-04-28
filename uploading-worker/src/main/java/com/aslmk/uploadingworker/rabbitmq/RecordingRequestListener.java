package com.aslmk.uploadingworker.rabbitmq;

import com.aslmk.uploadingworker.dto.RecordingStatusEvent;
import com.aslmk.uploadingworker.service.StreamUploaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecordingRequestListener {

    private final StreamUploaderService service;

    public RecordingRequestListener(StreamUploaderService service) {
        this.service = service;
    }

    @RabbitListener(queues = "${user.rabbitmq.queue.name}", concurrency = "${user.rabbitmq.listener.concurrency}")
    public void handleRecordingEvent(RecordingStatusEvent event) {
        log.info("Processing '{}' event: streamerId='{}', filename='{}'",
                event.getEventType(), event.getStreamerId(), event.getFilename());

        service.processUploadingRequest(event);
    }

}
