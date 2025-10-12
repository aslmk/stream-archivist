package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.dto.OAuthUserInfo;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.service.OAuthProviderStrategy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service("twitch")
public class OAuthTwitchStrategy implements OAuthProviderStrategy {

    private final OAuthAuthorizationService service;

    public OAuthTwitchStrategy(OAuthAuthorizationService service) {
        this.service = service;
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
        if (exp == null) return LocalDateTime.now().plusSeconds(60);

        if (exp > 100_000_000) return LocalDateTime
                .ofEpochSecond(exp, 0, ZoneOffset.UTC)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        return LocalDateTime.now().plusSeconds(exp);
    }
}
