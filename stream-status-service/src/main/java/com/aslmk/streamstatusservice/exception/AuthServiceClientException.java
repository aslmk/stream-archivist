package com.aslmk.streamstatusservice.exception;

public class AuthServiceClientException extends RuntimeException {
    public AuthServiceClientException(String message) {
        super(message);
    }

    public AuthServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
