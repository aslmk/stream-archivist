package com.aslmk.trackerservice.service.token;

import com.aslmk.trackerservice.domain.TwitchAppTokenEntity;
import com.aslmk.trackerservice.client.twitch.dto.TwitchAppAccessToken;

import java.util.Optional;

public interface TwitchAppTokenService {
    Optional<TwitchAppTokenEntity> getAppAccessToken();
    void save(TwitchAppAccessToken appToken);
    void update(TwitchAppTokenEntity currentAppToken, TwitchAppAccessToken newAppToken);
}
