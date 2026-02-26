package com.aslmk.trackerservice.exception;

public class KafkaEventSerializationException extends RuntimeException {
    public KafkaEventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
