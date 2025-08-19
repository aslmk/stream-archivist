package com.aslmk.recordingworker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class StreamRecordingException extends RuntimeException {
    public StreamRecordingException(String message) {
        super(message);
    }
}
