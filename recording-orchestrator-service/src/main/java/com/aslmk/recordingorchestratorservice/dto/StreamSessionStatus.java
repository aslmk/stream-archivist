package com.aslmk.recordingorchestratorservice.dto;

import lombok.Getter;

@Getter
public enum StreamSessionStatus {
    RECORDING("RECORDING"),
    UPLOADING("UPLOADING"),
    RECORDING_FAILED("RECORDING_FAILED"),
    UPLOADING_FAILED("UPLOADING_FAILED");

    private final String value;

    StreamSessionStatus(String value) {
        this.value = value;
    }

}
