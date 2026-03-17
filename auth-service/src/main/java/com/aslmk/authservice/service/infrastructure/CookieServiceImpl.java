package com.aslmk.authservice.service.infrastructure;

import com.aslmk.authservice.config.CookieProperties;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieServiceImpl implements CookieService {
    private final CookieProperties cookieProps;

    @Value("${user.cookie.jwt-access-token.name}")
    private String jwtAccessTokenName;

    @Value("${user.cookie.jwt-refresh-token.name}")
    private String jwtRefreshTokenName;

    public CookieServiceImpl(CookieProperties cookieProps) {
        this.cookieProps = cookieProps;
    }

    @Override
    public Cookie createAccessTokenCookie(String value) {
        Cookie cookie = new Cookie(jwtAccessTokenName, value);
        cookie.setPath(cookieProps.getPath());
        cookie.setHttpOnly(cookieProps.isHttpOnly());
        cookie.setSecure(cookieProps.isSecure());
        cookie.setDomain(cookieProps.getDomain());
        cookie.setMaxAge(cookieProps.getJwtAccessTokenMaxAge());

        return cookie;
    }

    @Override
    public Cookie createRefreshTokenCookie(String value) {
        Cookie cookie = new Cookie(jwtRefreshTokenName, value);
        cookie.setPath(cookieProps.getPath());
        cookie.setHttpOnly(cookieProps.isHttpOnly());
        cookie.setSecure(cookieProps.isSecure());
        cookie.setDomain(cookieProps.getDomain());
        cookie.setMaxAge(cookieProps.getJwtRefreshTokenMaxAge());

        return cookie;
    }
}
