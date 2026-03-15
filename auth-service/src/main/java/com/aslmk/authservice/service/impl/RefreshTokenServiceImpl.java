package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.entity.RefreshTokenEntity;
import com.aslmk.authservice.exception.InvalidRefreshTokenException;
import com.aslmk.authservice.exception.RefreshTokenExpiredException;
import com.aslmk.authservice.exception.RefreshTokenNotFoundException;
import com.aslmk.authservice.repository.RefreshTokenRepository;
import com.aslmk.authservice.service.RefreshTokenService;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${user.jwt-refresh-token.lifetime}")
    private Duration refreshTokenLifetime;

    private final RefreshTokenRepository repository;
    private final Clock clock;

    public RefreshTokenServiceImpl(RefreshTokenRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public String generate(UUID userId) {
        UUID refreshToken = UUID.randomUUID();

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .userId(userId)
                .tokenHash(hash(refreshToken.toString()))
                .expiresAt(LocalDateTime.now(clock).plusDays(refreshTokenLifetime.toDays()))
                .build();

        repository.save(entity);

        return refreshToken.toString();
    }

    @Override
    public RefreshTokenEntity validate(String token) {
        String hashedToken = hash(token);

        RefreshTokenEntity entity = repository.findByTokenHash(hashedToken)
                .orElseThrow(() ->
                        new RefreshTokenNotFoundException("Refresh token not found"));

        if (entity.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            repository.deleteByTokenHash(hashedToken);
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        return entity;
    }

    @Override
    public void delete(String token) {
        String hashedToken = hash(token);

        repository.deleteByTokenHash(hashedToken);
    }

    private String hash(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is empty");
        }

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashedToken = sha256.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.toBase64String(hashedToken);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
