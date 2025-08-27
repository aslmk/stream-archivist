package com.aslmk.trackerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnknownEventTypeException extends RuntimeException {
    public UnknownEventTypeException(String message) {
        super(message);
    }
}
