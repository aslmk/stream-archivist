package com.aslmk.authservice.service;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.UUID;

public interface OAuthProviderStrategy {
    UUID authorize(String providerUserId, OAuth2User oAuth2User, String accessToken, String refreshToken);
}
