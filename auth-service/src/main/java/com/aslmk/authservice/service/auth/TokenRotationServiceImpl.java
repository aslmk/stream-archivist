package com.aslmk.authservice.service.auth;

import com.aslmk.authservice.dto.JwtTokenPair;
import com.aslmk.authservice.domain.auth.RefreshTokenEntity;
import com.aslmk.authservice.service.token.JwtTokenService;
import com.aslmk.authservice.service.token.RefreshTokenService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class TokenRotationServiceImpl implements TokenRotationService {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public TokenRotationServiceImpl(JwtTokenService jwtTokenService,
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
