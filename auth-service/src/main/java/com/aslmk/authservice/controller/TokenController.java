package com.aslmk.authservice.controller;

import com.aslmk.authservice.dto.JwtTokenPair;
import com.aslmk.authservice.dto.JwtTokenPairInfo;
import com.aslmk.authservice.service.auth.TokenRotationService;
import com.aslmk.authservice.service.infrastructure.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth/tokens")
public class TokenController {

    private final TokenRotationService tokenRotationService;
    private final CookieService cookieService;

    public TokenController(TokenRotationService tokenRotationService, CookieService cookieService) {
        this.tokenRotationService = tokenRotationService;
        this.cookieService = cookieService;
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public JwtTokenPairInfo refreshToken(
            @CookieValue(name = "JWT_REFRESH_TOKEN") String token,
            HttpServletResponse httpResponse) {

        JwtTokenPair tokenPair = tokenRotationService.refreshTokens(token);

        Cookie accessTokenCookie = cookieService.createAccessTokenCookie(tokenPair.accessToken());
        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(tokenPair.refreshToken());

        httpResponse.addCookie(accessTokenCookie);
        httpResponse.addCookie(refreshTokenCookie);

        long accessTokenExpiresAt = Instant.now()
                .plusSeconds(accessTokenCookie.getMaxAge())
                .toEpochMilli();

        return new JwtTokenPairInfo(accessTokenExpiresAt);
    }
}
