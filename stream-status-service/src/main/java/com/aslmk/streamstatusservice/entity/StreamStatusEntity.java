package com.aslmk.streamstatusservice.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StreamStatusEntity {
    private UUID streamerId;
    private boolean isLive;
    private RecordingStatus recordingStatus;

    public StreamStatusEntity(UUID id) {
        this.streamerId = id;
        this.isLive = false;
        recordingStatus = RecordingStatus.NOT_RECORDING;
    }
}
