package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.dto.JwtTokenPair;
import com.aslmk.authservice.entity.RefreshTokenEntity;
import com.aslmk.authservice.service.AuthService;
import com.aslmk.authservice.service.JwtTokenService;
import com.aslmk.authservice.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(JwtTokenService jwtTokenService,
                           RefreshTokenService refreshTokenService) {
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public JwtTokenPair refreshTokens(String refreshToken) {
        RefreshTokenEntity entity = refreshTokenService.validate(refreshToken);

        UUID userId = entity.getUserId();

        String newAccessToken = jwtTokenService.generate(userId);
        String newRefreshToken = refreshTokenService.generate(userId);

        refreshTokenService.delete(refreshToken);

        return new JwtTokenPair(newAccessToken, newRefreshToken);
    }
}
