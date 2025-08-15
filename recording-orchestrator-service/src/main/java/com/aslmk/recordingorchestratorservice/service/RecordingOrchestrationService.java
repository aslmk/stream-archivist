package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.dto.RecordingRequestDto;

public interface RecordingOrchestrationService {
    void processRecordingRequest(RecordingRequestDto recordingRequestDto);
}
