package com.aslmk.authservice.config;

import com.aslmk.authservice.service.token.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class UserLogoutHandler implements LogoutHandler {

    private final RefreshTokenService service;

    public UserLogoutHandler(RefreshTokenService service) {
        this.service = service;
    }

    @Override
    public void logout(HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse,
                       Authentication authentication) {
        Cookie[] cookies = httpRequest.getCookies();

        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("JWT_REFRESH_TOKEN"))
                    .findFirst()
                    .ifPresent(cookie -> service.delete(cookie.getValue()));
        }
    }
}
