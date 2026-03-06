package com.aslmk.authservice.service;

import jakarta.servlet.http.Cookie;

public interface CookieService {
    Cookie createAccessTokenCookie(String value);
    Cookie createRefreshTokenCookie(String value);
}
