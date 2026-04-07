package com.aslmk.recordingworker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(Exception ex) {
        log.error("Unexpected error", ex);
        return new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), "Internal server error");
    }

    @ExceptionHandler(InvalidRecordingRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRecordingRequest(InvalidRecordingRequestException ex) {
        log.warn("Invalid recording request: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.INVALID_RECORDING_REQUEST.name(), ex.getMessage());
    }
}
