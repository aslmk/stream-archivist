package com.aslmk.trackerservice.domain;

import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import lombok.Getter;

@Getter
public enum EventType {
    STREAM_STARTED("STREAM_STARTED"),
    STREAM_ENDED("STREAM_ENDED");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public static EventType fromString(String value) {
        for (EventType eventType: EventType.values()) {
            if (eventType.value.equalsIgnoreCase(value)) {
                return eventType;
            }
        }

        throw new UnknownEventTypeException("Unknown event type: " + value);
    }
}
