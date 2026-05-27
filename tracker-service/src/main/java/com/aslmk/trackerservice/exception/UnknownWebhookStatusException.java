package com.aslmk.trackerservice.exception;

public class UnknownWebhookStatusException extends RuntimeException {
    public UnknownWebhookStatusException(String message) {
        super(message);
    }
}
