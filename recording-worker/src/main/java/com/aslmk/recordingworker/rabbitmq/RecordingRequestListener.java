package com.aslmk.recordingworker.rabbitmq;

import com.aslmk.recordingworker.dto.StreamLifecycleEvent;
import com.aslmk.recordingworker.service.StreamRecorderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecordingRequestListener {

    public final StreamRecorderService recordingService;

    public RecordingRequestListener(StreamRecorderService recordingService) {
        this.recordingService = recordingService;
    }

    @RabbitListener(queues = "${user.rabbitmq.queue.name}")
    public void handleRecordingRequest(StreamLifecycleEvent event) {
        log.info("Handling '{}' event: streamerId='{}'", event.getEventType(), event.getStreamerId());
        recordingService.recordStream(event);
    }
}
