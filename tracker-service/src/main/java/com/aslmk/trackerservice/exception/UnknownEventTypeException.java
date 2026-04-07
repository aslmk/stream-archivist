package com.aslmk.trackerservice.exception;

public class UnknownEventTypeException extends RuntimeException {
    public UnknownEventTypeException(String message) {
        super(message);
    }
}
