package com.aslmk.recordingorchestratorservice.messaging.rabbitmq;

import com.aslmk.recordingorchestratorservice.dto.RecordingStatusEvent;
import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMqService {

    @Value("${user.rabbitmq.recording-queue.name}")
    private String recordingQueueName;

    @Value("${user.rabbitmq.uploading-queue.name}")
    private String uploadingQueueName;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(StreamLifecycleEvent message) {
        log.info("Sending event '{}' to '{}' queue: streamerId='{}'",
                message.getEventType(), recordingQueueName, message.getStreamerId());

        rabbitTemplate.convertAndSend(recordingQueueName, message);
    }

    public void sendMessage(RecordingStatusEvent message) {
        log.info("Sending event '{}' to '{}' queue: streamerId='{}', filename='{}'",
                message.getEventType(),
                uploadingQueueName,
                message.getStreamerId(),
                message.getFilename());

        rabbitTemplate.convertAndSend(uploadingQueueName, message);
    }
}
