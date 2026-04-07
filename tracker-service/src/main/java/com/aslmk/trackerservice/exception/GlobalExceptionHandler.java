package com.aslmk.trackerservice.exception;

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

    @ExceptionHandler(StreamerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(StreamerNotFoundException ex) {
        log.warn("Streamer not found: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.STREAMER_NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(UnknownEventTypeException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleUnknownEventType(UnknownEventTypeException ex) {
        log.warn("Unknown event type: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.UNKNOWN_EVENT_TYPE.name(), ex.getMessage());
    }

    @ExceptionHandler(TrackingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTracking(TrackingException ex) {
        log.warn("Tracking error: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.INVALID_TRACKING_REQUEST.name(), ex.getMessage());
    }
}
