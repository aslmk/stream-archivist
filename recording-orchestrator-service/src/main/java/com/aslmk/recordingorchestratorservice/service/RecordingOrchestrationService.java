package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;

public interface RecordingOrchestrationService {
    void processRecordingRequest(StreamLifecycleEvent streamLifecycleEvent);
}
