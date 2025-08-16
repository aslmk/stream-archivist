package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.common.dto.RecordingRequestDto;

public interface RecordingOrchestrationService {
    void processRecordingRequest(RecordingRequestDto recordingRequestDto);
}
