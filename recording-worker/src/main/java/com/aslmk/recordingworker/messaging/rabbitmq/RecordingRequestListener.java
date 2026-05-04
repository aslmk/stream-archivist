package com.aslmk.recordingworker.messaging.rabbitmq;

import com.aslmk.recordingworker.dto.RecordStreamJob;
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

    @RabbitListener(queues = "${user.rabbitmq.queue.name}",
            concurrency = "${user.rabbitmq.listener.concurrency}")
    public void handleRecordStreamJob(RecordStreamJob job) {
        log.info("Handling stream recording job: streamId='{}'", job.getStreamId());
        recordingService.recordStream(job);
    }
}
