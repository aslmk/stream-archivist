package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.dto.CreateTokenDto;
import com.aslmk.authservice.entity.TokenEntity;
import com.aslmk.authservice.repository.TokenRepository;
import com.aslmk.authservice.service.TokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;

    public TokenServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public TokenEntity create(CreateTokenDto dto) {
        TokenEntity tokenEntity = TokenEntity.builder()
                .provider(dto.getProvider())
                .accessToken(dto.getAccessToken())
                .refreshToken(dto.getRefreshToken())
                .expiresAt(dto.getExpiresAt())
                .build();

        return tokenRepository.save(tokenEntity);
    }

    @Override
    public List<TokenEntity> getExpiredTokens(LocalDateTime now) {
        return tokenRepository.findAllByExpiresAtBefore(now);
    }

    @Override
    public void update(TokenEntity token) {
        tokenRepository.save(token);
    }
}
