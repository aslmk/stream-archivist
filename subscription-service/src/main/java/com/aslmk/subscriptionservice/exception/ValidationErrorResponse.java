package com.aslmk.subscriptionservice.exception;

import java.util.List;

public record ValidationErrorResponse(String error, String message, List<ValidationError> details) {
}
