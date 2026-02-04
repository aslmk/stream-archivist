package com.aslmk.streamstatusservice.exception;

public class SubscriptionServiceClientException extends RuntimeException {
    public SubscriptionServiceClientException(String message) {
        super(message);
    }

    public SubscriptionServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
