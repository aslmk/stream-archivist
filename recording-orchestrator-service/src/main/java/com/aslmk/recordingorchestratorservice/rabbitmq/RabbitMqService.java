package com.aslmk.recordingorchestratorservice.rabbitmq;

import com.aslmk.common.dto.RecordingRequestDto;
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

    public void sendMessage(RecordingRequestDto message) {
        log.info("Sending recording request to RabbitMQ: queue={}, streamerUsername={}, streamUrl={}",
                queueName,
                message.getStreamerUsername(),
                message.getStreamUrl()
        );
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
