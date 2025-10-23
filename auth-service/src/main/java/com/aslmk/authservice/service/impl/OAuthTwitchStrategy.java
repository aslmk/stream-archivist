package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.dto.OAuthUserInfo;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.service.OAuthProviderStrategy;
import com.aslmk.authservice.util.TokenTimeUtil;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

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
        LocalDateTime expiresAt = TokenTimeUtil.getExpiresAt(expAttribute, clock);

        OAuthUserInfo oAuthUserInfo = OAuthUserInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .providerUserId(providerUserId)
                .provider(ProviderName.twitch)
                .expiresAt(expiresAt)
                .build();

        service.authorize(oAuthUserInfo);
    }
}
