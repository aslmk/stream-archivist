package com.aslmk.recordingorchestratorservice.messaging.rabbitmq;

import com.aslmk.recordingorchestratorservice.dto.RecordStreamJob;
import com.aslmk.recordingorchestratorservice.dto.UploadStreamRecordJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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

    public boolean sendRecordJob(RecordStreamJob job) {
        CorrelationData correlationData = new CorrelationData(job.getStreamId().toString());
        rabbitTemplate.convertAndSend(recordingQueueName, job, correlationData);

        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(5, TimeUnit.SECONDS);
            return confirm != null && confirm.isAck();
        } catch (Exception e) {
            throw new RuntimeException("Did not receive confirmation in time: " + e.getMessage());
        }

    }

    public boolean sendUploadJob(UploadStreamRecordJob job) {
        CorrelationData correlationData = new CorrelationData(job.getStreamId().toString());
        rabbitTemplate.convertAndSend(uploadingQueueName, job, correlationData);

        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(5, TimeUnit.SECONDS);
            return confirm != null && confirm.isAck();
        } catch (Exception e) {
            throw new RuntimeException("Did not receive confirmation in time: " + e.getMessage());
        }
    }
}
