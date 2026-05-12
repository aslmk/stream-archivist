package com.aslmk.trackerservice.dto;

import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import lombok.Getter;

@Getter
public enum StreamLifecycleType {
    STREAM_STARTED("stream.online"),
    STREAM_ENDED("stream.offline");

    private final String value;

    StreamLifecycleType(String value) {
        this.value = value;
    }

    public static StreamLifecycleType fromValue(String value) {
        for (StreamLifecycleType type: StreamLifecycleType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new UnknownEventTypeException("Unknown event type: " + value);
    }
}
