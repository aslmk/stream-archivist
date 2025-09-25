package com.aslmk.uploadingworker.exception;

public class StreamUploadException extends RuntimeException {
    public StreamUploadException(String message) {
        super(message);
    }
    public StreamUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
