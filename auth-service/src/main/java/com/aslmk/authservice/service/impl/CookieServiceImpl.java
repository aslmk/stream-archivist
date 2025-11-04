package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.config.CookieProperties;
import com.aslmk.authservice.service.CookieService;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class CookieServiceImpl implements CookieService {
    private final CookieProperties cookieProps;

    public CookieServiceImpl(CookieProperties cookieProps) {
        this.cookieProps = cookieProps;
    }

    @Override
    public Cookie create(final String value) {
        Cookie cookie = new Cookie(cookieProps.getName(), value);
        cookie.setPath(cookieProps.getPath());
        cookie.setHttpOnly(cookieProps.isHttpOnly());
        cookie.setSecure(cookieProps.isSecure());
        cookie.setDomain(cookieProps.getDomain());
        cookie.setMaxAge(cookieProps.getMaxAge());
        return cookie;
    }
}
