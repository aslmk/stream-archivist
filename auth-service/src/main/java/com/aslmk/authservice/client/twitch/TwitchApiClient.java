package com.aslmk.authservice.client.twitch;

public interface TwitchApiClient {
    TwitchTokenRefreshResponse refreshTokens(String refreshToken);
}
