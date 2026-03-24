package com.aslmk.streamstatusservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StreamState {
    private UUID streamerId;
    private boolean isLive;
    private RecordingStatus recordingStatus;

    public StreamState(UUID id) {
        this.streamerId = id;
        this.isLive = false;
        recordingStatus = RecordingStatus.NOT_RECORDING;
    }
}
