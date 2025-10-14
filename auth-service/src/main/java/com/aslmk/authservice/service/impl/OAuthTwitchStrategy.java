package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.dto.OAuthUserInfo;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.service.OAuthProviderStrategy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service("twitch")
public class OAuthTwitchStrategy implements OAuthProviderStrategy {

    private final OAuthAuthorizationService service;
    private final Clock clock;

    public OAuthTwitchStrategy(OAuthAuthorizationService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @Override
    public void authorize(String providerUserId,
                          OAuth2User oAuth2User,
                          String accessToken,
                          String refreshToken) {

        Integer expAttribute = oAuth2User.getAttribute("exp");
        LocalDateTime expiresAt = getExpiresAt(expAttribute);

        OAuthUserInfo oAuthUserInfo = OAuthUserInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .providerUserId(providerUserId)
                .provider(ProviderName.twitch)
                .expiresAt(expiresAt)
                .build();

        service.authorize(oAuthUserInfo);
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
