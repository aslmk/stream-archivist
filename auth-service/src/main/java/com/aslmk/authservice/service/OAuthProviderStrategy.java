package com.aslmk.authservice.service;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuthProviderStrategy {
    void authorize(String providerUserId, OAuth2User oAuth2User, String accessToken, String refreshToken);
}
