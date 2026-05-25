package com.aslmk.archiveservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("The parameter '{}' type mismatch: expected='{}', actual='{}'",
                ex.getName(),
                ex.getRequiredType(),
                ex.getValue());

        String errorMessage = String
                .format("The parameter '%s' must be of type '%s', but the value '%s' was provided",
                        ex.getName(), ex.getRequiredType(), ex.getValue());
        return new ErrorResponse(ErrorCode.PARAMETER_MISMATCH.name(), errorMessage);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(Exception ex) {
        log.error("Unexpected error", ex);
        return new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), "Internal server error");
    }
}
