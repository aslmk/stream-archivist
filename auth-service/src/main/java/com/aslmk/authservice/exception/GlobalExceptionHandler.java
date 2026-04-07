package com.aslmk.authservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestCookieException;
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

    @ExceptionHandler(OAuthProviderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOAuthProviderNotFound(OAuthProviderNotFoundException ex) {
        log.warn("OAuth provider not found: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.OAUTH_PROVIDER_NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        log.warn("Invalid refresh token: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.REFRESH_TOKEN_INVALID.name(), ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        log.warn("Refresh token expired: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.REFRESH_TOKEN_EXPIRED.name(), ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        log.warn("Refresh token not found: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.REFRESH_TOKEN_NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestCookie(MissingRequestCookieException ex) {
        log.warn("Missing request cookie: {}", ex.getMessage());
        return new ErrorResponse(ErrorCode.REFRESH_TOKEN_COOKIE_MISSING.name(),
                "Refresh token cookie is not present");
    }
}
