package com.aslmk.authservice;

import com.aslmk.authservice.dto.OAuthUserInfo;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.service.impl.OAuthAuthorizationService;
import com.aslmk.authservice.service.impl.OAuthTwitchStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class OAuthTwitchStrategyUnitTests {

    @Mock
    private OAuthAuthorizationService service;

    @Mock
    private Clock clock;

    @InjectMocks
    private OAuthTwitchStrategy strategy;

    private static final String PRINCIPAL_NAME = "sub";
    private static final String PROVIDER_USER_ID = "123456789";
    private static final String EXP_ATTRIBUTE_NAME = "exp";
    private static final Integer EXP_ATTRIBUTE_VALUE = 1_759_895_200; // 13.10.2025 22:00:00:00
    private static final String ACCESS_TOKEN_VALUE = "40hg4g0q340gd0ghdgab0rg3404208f3h0";
    private static final String REFRESH_TOKEN_VALUE = "034gh0ehga9dbg03b2g032g380h23rh39r";

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2025, 10, 13,
            22, 0, 0, 0,
            ZoneId.of("UTC")
    );

    private Map<String, Object> OAuth2Attributes;

    @BeforeEach
    void setUp() {
        OAuth2Attributes = new HashMap<>();
        OAuth2Attributes.put(PRINCIPAL_NAME, PROVIDER_USER_ID);
        OAuth2Attributes.put(EXP_ATTRIBUTE_NAME, EXP_ATTRIBUTE_VALUE);
    }

    @Test
    void authorize_should_callOAuthAuthorizationService() {
        Mockito.when(clock.getZone()).thenReturn(NOW.getZone());
        Mockito.when(clock.instant()).thenReturn(NOW.toInstant());

        OAuth2Attributes.put(EXP_ATTRIBUTE_NAME, null);
        OAuth2User oAuth2User = buildOAuth2User(OAuth2Attributes);

        OAuthUserInfo expected = OAuthUserInfo.builder()
                .accessToken(ACCESS_TOKEN_VALUE)
                .refreshToken(REFRESH_TOKEN_VALUE)
                .providerUserId(PROVIDER_USER_ID)
                .expiresAt(NOW.plusSeconds(60).toLocalDateTime())
                .provider(ProviderName.twitch)
                .build();

        strategy.authorize(PROVIDER_USER_ID, oAuth2User, ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE);

        ArgumentCaptor<OAuthUserInfo> captor = ArgumentCaptor.forClass(OAuthUserInfo.class);
        Mockito.verify(service).authorize(captor.capture());
        OAuthUserInfo actual = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken()),
                () -> Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken()),
                () -> Assertions.assertEquals(expected.getProvider(), actual.getProvider()),
                () -> Assertions.assertEquals(expected.getProviderUserId(), actual.getProviderUserId()),
                () -> Assertions.assertEquals(expected.getExpiresAt(), actual.getExpiresAt())
        );
    }

    private OAuth2User buildOAuth2User(Map<String, Object> attributes) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                PRINCIPAL_NAME);
    }
}
