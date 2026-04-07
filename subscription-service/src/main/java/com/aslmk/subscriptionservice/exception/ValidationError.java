package com.aslmk.subscriptionservice.exception;

public record ValidationError(String field, String message) {
}
