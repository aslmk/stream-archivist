package com.aslmk.authservice.client.twitch;

import com.aslmk.authservice.dto.TwitchTokenRefreshResponse;

public interface TwitchApiClient {
    TwitchTokenRefreshResponse refreshTokens(String refreshToken);
}
