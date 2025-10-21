package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.client.twitch.TwitchApiClient;
import com.aslmk.authservice.client.twitch.TwitchTokenRefreshResponse;
import com.aslmk.authservice.entity.TokenEntity;
import com.aslmk.authservice.exception.TwitchApiClientException;
import com.aslmk.authservice.service.TokenService;
import com.aslmk.authservice.service.TokenUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TokenUpdateServiceImpl implements TokenUpdateService {
    private final Clock clock;
    private final TokenService tokenService;
    private final TwitchApiClient apiClient;

    public TokenUpdateServiceImpl(Clock clock, TokenService tokenService, TwitchApiClient apiClient) {
        this.clock = clock;
        this.tokenService = tokenService;
        this.apiClient = apiClient;
    }

    @Override
    public void updateExpiredTokens() {
        List<TokenEntity> expiredTokens = tokenService.getExpiredTokens(LocalDateTime.now(clock).plusMinutes(5));
        int updatedCount = 0;

        for (TokenEntity expiredToken : expiredTokens) {
            try {
                TwitchTokenRefreshResponse response = apiClient.refreshTokens(expiredToken.getRefreshToken());
                expiredToken.setRefreshToken(response.getRefreshToken());
                expiredToken.setAccessToken(response.getAccessToken());
                expiredToken.setExpiresAt(getExpiresAt(response.getExpiresIn()));
                tokenService.update(expiredToken);
                updatedCount++;
            } catch (TwitchApiClientException e) {
                UUID userId = expiredToken.getProvider().getUser().getId();
                log.warn("Failed to update expired tokens for user with id {}: {}", userId, e.getMessage());
            }
        }

        if (!expiredTokens.isEmpty()) {
            log.info("Successfully updated {} expired tokens", updatedCount);
        }
    }

    private LocalDateTime getExpiresAt(Integer exp) {
        if (exp == null) return LocalDateTime.now(clock).plusSeconds(60);

        if (isUnixTimestamp(exp)) return LocalDateTime
                .ofEpochSecond(exp, 0, ZoneOffset.UTC)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        return LocalDateTime.now(clock).plusSeconds(exp);
    }

    private boolean isUnixTimestamp(Integer value) {
        return value > 100_000_000;
    }
}
