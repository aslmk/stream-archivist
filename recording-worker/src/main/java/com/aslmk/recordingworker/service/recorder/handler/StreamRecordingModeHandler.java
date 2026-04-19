package com.aslmk.recordingworker.service.recorder.handler;

import com.aslmk.recordingworker.service.recorder.RecordingPayload;

public interface StreamRecordingModeHandler {
    boolean support(String mode);
    void run(RecordingPayload payload);
}
