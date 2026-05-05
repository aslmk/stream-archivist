package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.JobLogEntity;
import com.aslmk.recordingorchestratorservice.domain.JobPayload;
import com.aslmk.recordingorchestratorservice.domain.JobType;
import com.aslmk.recordingorchestratorservice.dto.JobLogStatus;

import java.util.List;
import java.util.UUID;

public interface JobLogService {
    void updateStatus(UUID jobId, JobLogStatus jobStatus);
    List<JobLogEntity> getAllPendingJobs();
    void save(JobPayload payload, JobType jobType);
}
