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

    public static StreamSessionStatus fromValue(String value) {
        for (StreamSessionStatus status: StreamSessionStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new UnsupportedOperationException("Unknown stream session status: " + value);
    }
}
