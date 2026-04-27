package com.aslmk.recordingworker.exception;

public class StitchingServiceException extends RuntimeException {

    public StitchingServiceException(String message) {
        super(message);
    }

    public StitchingServiceException(String message, Throwable e) {
        super(message, e);
    }
}
