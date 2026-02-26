package com.aslmk.uploadingworker.exception;

public class KafkaEventDeserializationException extends RuntimeException {
    public KafkaEventDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
