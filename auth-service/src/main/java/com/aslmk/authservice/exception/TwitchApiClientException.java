package com.aslmk.authservice.exception;

public class TwitchApiClientException extends RuntimeException {
    public TwitchApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
