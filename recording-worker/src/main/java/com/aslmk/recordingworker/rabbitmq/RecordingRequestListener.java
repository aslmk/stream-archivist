package com.aslmk.recordingworker.rabbitmq;

import com.aslmk.common.dto.RecordingRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecordingRequestListener {

    @RabbitListener(queues = "${user.rabbitmq.queue.name}")
    public void handleRecordingRequest(RecordingRequestDto request) {
        log.info("Received request: \n{}", request);
    }
}
