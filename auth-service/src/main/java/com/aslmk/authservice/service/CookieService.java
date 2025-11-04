package com.aslmk.authservice.service;

import jakarta.servlet.http.Cookie;

public interface CookieService {
    Cookie create(String value);
}
