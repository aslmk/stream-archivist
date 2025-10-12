package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.exception.OAuthProviderNotFoundException;
import com.aslmk.authservice.service.OAuthProviderStrategy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OAuthAuthorizationOrchestrator {

    private final Map<String, OAuthProviderStrategy> providerStrategies;

    public OAuthAuthorizationOrchestrator(Map<String, OAuthProviderStrategy> providerStrategies) {
        this.providerStrategies = providerStrategies;
    }

    public void authorize(String providerUserId, OAuth2AuthorizedClient client, OAuth2User oauth2User) {
        String providerName = client.getClientRegistration().getRegistrationId();

        String accessToken = getAccessToken(client.getAccessToken());
        String refreshToken = getRefreshToken(client.getRefreshToken());

        OAuthProviderStrategy strategy = getProvider(providerName);
        strategy.authorize(providerUserId, oauth2User, accessToken, refreshToken);
    }

    private OAuthProviderStrategy getProvider(String providerName) {
        OAuthProviderStrategy strategy = providerStrategies.get(providerName);
        if (strategy == null) {
            throw new OAuthProviderNotFoundException(
                    String.format("No OAuth provider strategy found for provider: %s", providerName)
            );
        }
        return strategy;
    }

    private String getAccessToken(OAuth2AccessToken oAuth2AccessToken) {
        return oAuth2AccessToken.getTokenValue();
    }

    private String getRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        return oAuth2RefreshToken == null ? "" : oAuth2RefreshToken.getTokenValue();
    }
}
