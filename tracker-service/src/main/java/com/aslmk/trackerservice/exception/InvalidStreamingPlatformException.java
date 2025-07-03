package com.aslmk.trackerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStreamingPlatformException extends RuntimeException {
    public InvalidStreamingPlatformException(String message) {
        super(message);
    }
}
