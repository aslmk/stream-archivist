package com.aslmk.trackerservice.exception;

public class TwitchApiClientException extends RuntimeException {
    public TwitchApiClientException(String message) {
        super(message);
    }
    public TwitchApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
