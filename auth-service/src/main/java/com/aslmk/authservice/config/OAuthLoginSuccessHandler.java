package com.aslmk.authservice.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuthLoginSuccessHandler(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
        String principalName = oauth2Token.getName();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(registrationId, principalName);


        if (authorizedClient != null) {
            OAuth2AccessToken oAuth2AccessToken = authorizedClient.getAccessToken();
            OAuth2RefreshToken oAuth2RefreshToken = authorizedClient.getRefreshToken();

            // TODO: add Database support to store tokens
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
