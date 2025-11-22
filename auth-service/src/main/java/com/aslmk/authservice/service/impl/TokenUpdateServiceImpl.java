package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.client.twitch.TwitchApiClient;
import com.aslmk.authservice.client.twitch.TwitchTokenRefreshResponse;
import com.aslmk.authservice.entity.TokenEntity;
import com.aslmk.authservice.exception.TwitchApiClientException;
import com.aslmk.authservice.service.TokenService;
import com.aslmk.authservice.service.TokenUpdateService;
import com.aslmk.authservice.util.TokenTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
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
                expiredToken.setExpiresAt(TokenTimeUtil.getExpiresAt(response.getExpiresIn(), clock));
                tokenService.update(expiredToken);
                updatedCount++;
            } catch (TwitchApiClientException e) {
                UUID userId = expiredToken.getProvider().getUser().getId();
                log.warn("Failed to update expired token '{}' for user '{}'",expiredToken.getId(), userId);
            }
        }

        if (!expiredTokens.isEmpty()) {
            log.info("Successfully updated {} expired tokens", updatedCount);
        }
    }

    @Override
    public void updateIfExpired(TokenEntity token) {
        if (token.getExpiresAt().isBefore(LocalDateTime.now(clock).plusMinutes(5))) {
            TwitchTokenRefreshResponse response = apiClient.refreshTokens(token.getRefreshToken());
            token.setRefreshToken(response.getRefreshToken());
            token.setAccessToken(response.getAccessToken());
            token.setExpiresAt(TokenTimeUtil.getExpiresAt(response.getExpiresIn(), clock));
            tokenService.update(token);
        }
    }
}
