package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.entity.TwitchAppTokenEntity;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchAppAccessToken;

import java.util.Optional;

public interface TwitchAppTokenService {
    Optional<TwitchAppTokenEntity> getAppAccessToken();
    void save(TwitchAppAccessToken appToken);
    void update(TwitchAppTokenEntity currentAppToken, TwitchAppAccessToken newAppToken);
}
