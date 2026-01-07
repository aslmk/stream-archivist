package com.aslmk.subscriptionservice.exception;

public class TrackerServiceClientException extends RuntimeException {
    public TrackerServiceClientException(String message) {
        super(message);
    }
    public TrackerServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
