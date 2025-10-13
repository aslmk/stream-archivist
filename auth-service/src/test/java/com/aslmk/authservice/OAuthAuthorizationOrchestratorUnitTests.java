package com.aslmk.authservice;

import com.aslmk.authservice.exception.OAuthProviderNotFoundException;
import com.aslmk.authservice.service.OAuthProviderStrategy;
import com.aslmk.authservice.service.impl.OAuthAuthorizationOrchestrator;
import com.aslmk.authservice.service.impl.OAuthTwitchStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class OAuthAuthorizationOrchestratorUnitTests {

    private static final String PROVIDER_NAME = "twitch";
    private static final String PRINCIPAL_NAME = "sub";
    private static final String EMPTY_STRING = "";
    private static final String PROVIDER_USER_ID = "123456789";
    private static final String ACCESS_TOKEN_VALUE = "40hg4g0q340gd0ghdgab0rg3404208f3h0";
    private static final String REFRESH_TOKEN_VALUE = "034gh0ehga9dbg03b2g032g380h23rh39r";

    private final OAuth2User oAuth2User = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of(PRINCIPAL_NAME, PROVIDER_USER_ID),
            PRINCIPAL_NAME);


    private final ClientRegistration twitchClientRegistration = buildClientRegistration(PROVIDER_NAME);

    private final OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            ACCESS_TOKEN_VALUE,
            Instant.now(),
            Instant.now().plusSeconds(900) // 15 minutes
    );
    private final OAuth2RefreshToken oAuth2RefreshToken = new OAuth2RefreshToken(
            REFRESH_TOKEN_VALUE,
            Instant.now()
    );

    private OAuthAuthorizationOrchestrator orchestrator;
    private OAuthTwitchStrategy twitchStrategy;
    private String accessToken;
    private String refreshToken;

    private final OAuth2AuthorizedClient validClient = new OAuth2AuthorizedClient(twitchClientRegistration,
            PRINCIPAL_NAME,
            oAuth2AccessToken,
            oAuth2RefreshToken);

    @BeforeEach
    void setUp() {
        accessToken = oAuth2AccessToken.getTokenValue();
        refreshToken = oAuth2RefreshToken.getTokenValue();

        twitchStrategy = Mockito.mock(OAuthTwitchStrategy.class);

        Map<String, OAuthProviderStrategy> providers = new HashMap<>();
        providers.put(PROVIDER_NAME, twitchStrategy);

        orchestrator = new OAuthAuthorizationOrchestrator(providers);
    }

    @Test
    void getProvider_should_throwOAuthProviderNotFoundException_when_providerNotFound() {
        ClientRegistration invalidClientRegistration = buildClientRegistration("unknown-provider");

        OAuth2AuthorizedClient invalidClient = buildAuthorizedClient(invalidClientRegistration,
                PRINCIPAL_NAME,
                oAuth2AccessToken,
                oAuth2RefreshToken);

        Assertions.assertThrows(OAuthProviderNotFoundException.class,
                () -> orchestrator.authorize(oAuth2User.getName(), invalidClient, oAuth2User));
    }

    @Test
    void authorize_should_callOAuthTwitchStrategy() {
        orchestrator.authorize(oAuth2User.getName(), validClient, oAuth2User);

        Mockito.verify(twitchStrategy).authorize(oAuth2User.getName(), oAuth2User, accessToken, refreshToken);
    }

    @Test
    void getAccessToken_should_returnAccessToken() {
        orchestrator.authorize(oAuth2User.getName(), validClient, oAuth2User);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(twitchStrategy).authorize(Mockito.anyString(),
                Mockito.any(),
                captor.capture(),
                Mockito.anyString());

        Assertions.assertEquals(ACCESS_TOKEN_VALUE, captor.getValue());
    }

    @Test
    void getRefreshToken_should_returnRefreshToken_when_refreshTokenIsNotNull() {
        orchestrator.authorize(oAuth2User.getName(), validClient, oAuth2User);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(twitchStrategy).authorize(Mockito.anyString(),
                Mockito.any(),
                Mockito.anyString(),
                captor.capture());

        Assertions.assertEquals(REFRESH_TOKEN_VALUE, captor.getValue());
    }

    @Test
    void getRefreshToken_should_returnEmptyString_when_refreshTokenIsNull() {
        OAuth2AuthorizedClient invalidClient = buildAuthorizedClient(twitchClientRegistration,
                PRINCIPAL_NAME,
                oAuth2AccessToken,
                null);

        orchestrator.authorize(oAuth2User.getName(), invalidClient, oAuth2User);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(twitchStrategy).authorize(Mockito.anyString(),
                Mockito.any(),
                Mockito.anyString(),
                captor.capture());

        Assertions.assertEquals(EMPTY_STRING, captor.getValue());
    }

    private ClientRegistration buildClientRegistration(String providerName) {
        return ClientRegistration
                .withRegistrationId(providerName)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("client")
                .redirectUri("http://example.com")
                .scope("read")
                .authorizationUri("http://example.com/authorize")
                .tokenUri("http://example.com/token")
                .userInfoUri("http://example.com/userinfo")
                .clientSecret("super_secret")
                .userNameAttributeName(PRINCIPAL_NAME)
                .clientName(providerName)
                .build();
    }

    private OAuth2AuthorizedClient buildAuthorizedClient(ClientRegistration clientRegistration,
                                                         String principalName,
                                                         OAuth2AccessToken accessToken,
                                                         OAuth2RefreshToken refreshToken) {
        return new OAuth2AuthorizedClient(clientRegistration,
                principalName,
                accessToken,
                refreshToken);
    }
}
