package com.aslmk.trackerservice.service.token;

import com.aslmk.trackerservice.client.twitch.dto.TwitchAppAccessToken;
import com.aslmk.trackerservice.domain.TwitchAppTokenEntity;
import com.aslmk.trackerservice.repository.TwitchAppTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;

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
        return repository.findTopByOrderByIdAsc();
    }

    @Override
    public void save(TwitchAppAccessToken appToken) {
        TwitchAppTokenEntity token = TwitchAppTokenEntity.builder()
                .accessToken(appToken.getAccessToken())
                .expiresAt(TwitchAppTokenExpirationResolver.getExpiresAt(appToken.getExpiresIn(), clock))
                .tokenType(appToken.getTokenType())
                .build();

        repository.save(token);
    }

    @Override
    public void update(TwitchAppTokenEntity currentAppToken, TwitchAppAccessToken newAppToken) {
        currentAppToken.setAccessToken(newAppToken.getAccessToken());
        currentAppToken.setExpiresAt(TwitchAppTokenExpirationResolver
                .getExpiresAt(newAppToken.getExpiresIn(), clock)
        );
        currentAppToken.setTokenType(newAppToken.getTokenType());
        repository.save(currentAppToken);
    }
}
