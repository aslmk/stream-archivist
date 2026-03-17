package com.aslmk.authservice.service.infrastructure;

import jakarta.servlet.http.Cookie;

public interface CookieService {
    Cookie createAccessTokenCookie(String value);
    Cookie createRefreshTokenCookie(String value);
}
