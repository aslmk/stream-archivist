package com.aslmk.gatewayservice.exception;

public class JwtCookieNotFoundException extends RuntimeException {
    public JwtCookieNotFoundException(String message) {
        super(message);
    }
}
