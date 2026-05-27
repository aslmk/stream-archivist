package com.aslmk.recordingorchestratorservice.exception;

public class UnknownJobTypeException extends RuntimeException {
    public UnknownJobTypeException(String message) {
        super(message);
    }
}
