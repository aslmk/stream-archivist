package com.aslmk.recordingworker.exception;

public class KafkaEventSerializationException extends RuntimeException {
    public KafkaEventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
