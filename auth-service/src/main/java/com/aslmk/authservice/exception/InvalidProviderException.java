package com.aslmk.authservice.exception;

public class InvalidProviderException extends RuntimeException {
    public InvalidProviderException(String message) {
        super(message);
    }
}
