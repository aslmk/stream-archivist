package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.JobLogEntity;
import com.aslmk.recordingorchestratorservice.domain.JobPayload;
import com.aslmk.recordingorchestratorservice.domain.JobType;
import com.aslmk.recordingorchestratorservice.dto.JobLogStatus;
import com.aslmk.recordingorchestratorservice.repository.JobLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JobLogServiceImpl implements JobLogService {

    private final JobLogRepository repository;

    public JobLogServiceImpl(JobLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(JobPayload payload, JobType jobType) {
        JobLogEntity entity = JobLogEntity.builder()
                .status(JobLogStatus.PENDING.name())
                .jobType(jobType.name())
                .payload(payload)
                .build();

        repository.save(entity);
    }

    @Override
    public void updateStatus(UUID jobId, JobLogStatus jobStatus) {
        repository.updateStatus(jobId, jobStatus.name());
    }

    @Override
    public List<JobLogEntity> getAllPendingJobs() {
        return repository.getAllPendingJobs();
    }
}
