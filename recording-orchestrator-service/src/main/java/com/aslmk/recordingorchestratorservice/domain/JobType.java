package com.aslmk.recordingorchestratorservice.domain;

import com.aslmk.recordingorchestratorservice.exception.UnknownJobTypeException;
import lombok.Getter;

@Getter
public enum JobType {
    RECORD("RECORD"),
    UPLOAD("UPLOAD");

    private final String value;

    JobType(String value) {
        this.value = value;
    }

    public static JobType fromString(String value) {
        for (JobType jobType: JobType.values()) {
            if (jobType.value.equalsIgnoreCase(value)) {
                return jobType;
            }
        }

        throw new UnknownJobTypeException("Unknown job type: " + value);
    }
}
