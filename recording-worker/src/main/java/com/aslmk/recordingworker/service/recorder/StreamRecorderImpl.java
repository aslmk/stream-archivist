package com.aslmk.recordingworker.service.recorder;

import com.aslmk.recordingworker.service.recorder.handler.StreamRecordingModeHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StreamRecorderImpl implements StreamRecorder {

    @Value("${user.recording.mode}")
    private String RECORDING_MODE;

    private final List<StreamRecordingModeHandler> recordingHandlers;

    public StreamRecorderImpl(List<StreamRecordingModeHandler> recordingHandlers) {
        this.recordingHandlers = recordingHandlers;
    }

    public void record(RecordingPayload payload) {
        for (StreamRecordingModeHandler handler : recordingHandlers) {
            if (handler.support(RECORDING_MODE)) {
                handler.run(payload);
                return;
            }
        }

        throw new IllegalArgumentException("Unsupported recording mode: " + RECORDING_MODE);
    }
}
