package com.aslmk.authservice.controller;

import com.aslmk.authservice.dto.JwtTokenPair;
import com.aslmk.authservice.service.auth.TokenRotationService;
import com.aslmk.authservice.service.infrastructure.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tokens")
public class TokenController {

    private final TokenRotationService tokenRotationService;
    private final CookieService cookieService;

    public TokenController(TokenRotationService tokenRotationService, CookieService cookieService) {
        this.tokenRotationService = tokenRotationService;
        this.cookieService = cookieService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(
            @CookieValue(name = "JWT_REFRESH_TOKEN") String token,
            HttpServletResponse httpResponse) {

        JwtTokenPair tokenPair = tokenRotationService.refreshTokens(token);

        Cookie accessTokenCookie = cookieService.createAccessTokenCookie(tokenPair.accessToken());
        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(tokenPair.refreshToken());

        httpResponse.addCookie(accessTokenCookie);
        httpResponse.addCookie(refreshTokenCookie);

        return ResponseEntity.noContent().build();
    }
}
