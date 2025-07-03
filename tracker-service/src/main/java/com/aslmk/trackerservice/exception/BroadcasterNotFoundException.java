package com.aslmk.trackerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BroadcasterNotFoundException extends RuntimeException {
    public BroadcasterNotFoundException(String message) {
        super(message);
    }
}