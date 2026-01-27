package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.common.dto.StreamLifecycleEvent;

public interface RecordingOrchestrationService {
    void processRecordingRequest(StreamLifecycleEvent streamLifecycleEvent);
}
