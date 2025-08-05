package com.aslmk.trackerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StreamerNotFoundException extends RuntimeException {
    public StreamerNotFoundException(String message) {
        super(message);
    }
}