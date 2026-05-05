package com.aslmk.recordingorchestratorservice.scheduler;

import com.aslmk.recordingorchestratorservice.domain.JobLogEntity;
import com.aslmk.recordingorchestratorservice.domain.JobType;
import com.aslmk.recordingorchestratorservice.dto.JobLogStatus;
import com.aslmk.recordingorchestratorservice.dto.RecordStreamJob;
import com.aslmk.recordingorchestratorservice.dto.UploadStreamRecordJob;
import com.aslmk.recordingorchestratorservice.messaging.rabbitmq.RabbitMqService;
import com.aslmk.recordingorchestratorservice.service.JobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PublishPendingJobs {
    private final JobLogService service;
    private final RabbitMqService rabbitMqService;

    public PublishPendingJobs(JobLogService service, RabbitMqService rabbitMqService) {
        this.service = service;
        this.rabbitMqService = rabbitMqService;
    }


    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void publishPendingJobs() {
        List<JobLogEntity> pendingJobs = service.getAllPendingJobs();

        for (JobLogEntity pendingJob: pendingJobs) {
            JobType jobType = JobType.fromString(pendingJob.getJobType());
            boolean result = false;

            try {
                if (jobType.equals(JobType.RECORD)) {
                    result = rabbitMqService.sendRecordJob((RecordStreamJob) pendingJob.getPayload());
                } else if (jobType.equals(JobType.UPLOAD)) {
                    result = rabbitMqService.sendUploadJob((UploadStreamRecordJob) pendingJob.getPayload());
                }

                if (result) {
                    service.updateStatus(pendingJob.getId(), JobLogStatus.SENT_TO_BROKER);
                }
            } catch (AmqpException e) {
                log.warn("Failed to send '{}' job to the RabbitMQ broker: jobId='{}'",
                        jobType, pendingJob.getId());
            }
        }
    }
}
