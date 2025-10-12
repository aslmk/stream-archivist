package com.aslmk.authservice.exception;

public class OAuthProviderNotFoundException extends RuntimeException {
    public OAuthProviderNotFoundException(String message) {
        super(message);
    }
}
