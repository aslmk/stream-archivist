package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.entity.TwitchAppTokenEntity;
import com.aslmk.trackerservice.repository.TwitchAppTokenRepository;
import com.aslmk.trackerservice.service.TwitchAppTokenService;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchAppAccessToken;
import com.aslmk.trackerservice.streamingPlatform.twitch.resolver.TwitchAppTokenExpirationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class TwitchAppTokenServiceImpl implements TwitchAppTokenService {
    private final TwitchAppTokenRepository repository;
    private final Clock clock;

    public TwitchAppTokenServiceImpl(TwitchAppTokenRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Optional<TwitchAppTokenEntity> getAppAccessToken() {
        log.debug("Fetching current App Access Token from database");
        Optional<TwitchAppTokenEntity> token = repository.findFirst();

        if (token.isPresent()) {
            LocalDateTime expiresAt = token.get().getExpiresAt();
            log.info("App Access Token found, expiresAt = {}", expiresAt);
        } else {
            log.warn("No App Access Token found in database");
        }

        return token;
    }

    @Override
    public void save(TwitchAppAccessToken appToken) {
        log.info("Saving new App Access Token, expiresIn = {}", appToken.getExpiresIn());

        TwitchAppTokenEntity token = TwitchAppTokenEntity.builder()
                .accessToken(appToken.getAccessToken())
                .expiresAt(TwitchAppTokenExpirationResolver.getExpiresAt(appToken.getExpiresIn(), clock))
                .tokenType(appToken.getTokenType())
                .build();

        repository.save(token);
        log.info("New App Access Token successfully saved, expiresAt = {}", token.getExpiresAt());
    }

    @Override
    public void update(TwitchAppTokenEntity currentAppToken, TwitchAppAccessToken newAppToken) {
        log.info("Updating existing App Access Token, expiresIn = {}", newAppToken.getExpiresIn());

        currentAppToken.setAccessToken(newAppToken.getAccessToken());
        currentAppToken.setExpiresAt(TwitchAppTokenExpirationResolver
                .getExpiresAt(newAppToken.getExpiresIn(), clock)
        );
        currentAppToken.setTokenType(newAppToken.getTokenType());
        repository.save(currentAppToken);
        log.info("App Access Token successfully updated, new expiresAt = {}", currentAppToken.getExpiresAt());
    }
}
