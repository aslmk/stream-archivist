package com.aslmk.recordingorchestratorservice.rabbitmq;

import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMqService {

    @Value("${user.rabbitmq.queue.name}")
    private String queueName;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(StreamLifecycleEvent message) {
        log.info("Sending event '{}' to '{}' queue: streamerId='{}'",
                message.getEventType(), queueName, message.getStreamerId());
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
