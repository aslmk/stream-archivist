package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.dto.RecordedPartEvent;
import com.aslmk.recordingorchestratorservice.dto.RecordingStatusEvent;
import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;

public interface RecordingOrchestrationService {
    void processStreamEvent(StreamLifecycleEvent event);
    void processRecordingEvent(RecordingStatusEvent event);
    void processRecordingPartEvent(RecordedPartEvent event);
}