package com.aslmk.recordingorchestratorservice.exception;

public class StreamSessionNotFoundException extends RuntimeException {
    public StreamSessionNotFoundException(String message) {
        super(message);
    }
}
