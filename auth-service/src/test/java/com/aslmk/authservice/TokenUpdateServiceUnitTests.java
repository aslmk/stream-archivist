package com.aslmk.authservice;

import com.aslmk.authservice.client.twitch.TwitchApiClient;
import com.aslmk.authservice.client.twitch.TwitchTokenRefreshResponse;
import com.aslmk.authservice.entity.ProviderEntity;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.entity.TokenEntity;
import com.aslmk.authservice.entity.UserEntity;
import com.aslmk.authservice.exception.TwitchApiClientException;
import com.aslmk.authservice.service.TokenService;
import com.aslmk.authservice.service.impl.TokenUpdateServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class TokenUpdateServiceUnitTests {

    @InjectMocks
    private TokenUpdateServiceImpl tokenUpdateService;
    @Mock
    private TokenService tokenService;
    @Mock
    private Clock clock;
    @Mock
    private TwitchApiClient apiClient;

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2025, 10, 21,
            18, 0, 0, 0,
            ZoneId.of("UTC")
    );

    private static final String OLD_ACCESS_TOKEN_VALUE = "40hg4g0q340gd0ghdgab0rg3404208f3h0";
    private static final String OLD_REFRESH_TOKEN_VALUE = "034gh0ehga9dbg03b2g032g380h23rh39r";
    private static final String NEW_ACCESS_TOKEN_VALUE = "new_access_tokeng425-g254g2go45pggm";
    private static final String NEW_REFRESH_TOKEN_VALUE = "new_refresh_tokeng2op5g245gpo4m5gp";
    private static final String PROVIDER_USER_ID = "123456789";

    private TwitchTokenRefreshResponse twitchResponse;
    private List<TokenEntity> expiredTokens;

    @BeforeEach
    void setUp() {
        Mockito.when(clock.instant()).thenReturn(NOW.toInstant());
        Mockito.when(clock.getZone()).thenReturn(NOW.getZone());

        TokenEntity expiredToken = generateTokenEntity(OLD_ACCESS_TOKEN_VALUE, OLD_REFRESH_TOKEN_VALUE);

        expiredTokens = new ArrayList<>();
        expiredTokens.add(expiredToken);

        twitchResponse = new TwitchTokenRefreshResponse();
        twitchResponse.setAccessToken(NEW_ACCESS_TOKEN_VALUE);
        twitchResponse.setRefreshToken(NEW_REFRESH_TOKEN_VALUE);
        twitchResponse.setScopes(List.of("scope1", "scope2"));
        twitchResponse.setExpiresIn(3600);
    }

    @Test
    void should_updateExpiredTokens() {
        Mockito.when(tokenService.getExpiredTokens(NOW.toLocalDateTime().plusMinutes(5)))
                .thenReturn(expiredTokens);

        Mockito.when(apiClient.refreshTokens(Mockito.anyString())).thenReturn(twitchResponse);

        tokenUpdateService.updateExpiredTokens();

        ArgumentCaptor<TokenEntity> captor = ArgumentCaptor.forClass(TokenEntity.class);
        Mockito.verify(tokenService).update(captor.capture());
        TokenEntity actual = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(NEW_ACCESS_TOKEN_VALUE, actual.getAccessToken()),
                () -> Assertions.assertEquals(NEW_REFRESH_TOKEN_VALUE, actual.getRefreshToken()),
                () -> Assertions.assertEquals(NOW.toLocalDateTime().plusSeconds(3600), actual.getExpiresAt())
        );

    }

    @Test
    void should_updateOneOutOfTwoExpiredTokens_when_apiClientThrowsException() {
        TokenEntity expiredToken2 = generateTokenEntity(OLD_ACCESS_TOKEN_VALUE, OLD_REFRESH_TOKEN_VALUE);
        expiredTokens.add(expiredToken2);

        Mockito.when(tokenService.getExpiredTokens(NOW.toLocalDateTime().plusMinutes(5)))
                .thenReturn(expiredTokens);

        Mockito.when(apiClient.refreshTokens(Mockito.anyString()))
                .thenReturn(twitchResponse)
                .thenThrow(TwitchApiClientException.class);

        tokenUpdateService.updateExpiredTokens();

        Mockito.verify(tokenService, Mockito.times(1)).update(Mockito.any());
    }

    @Test
    void should_doNoting_when_noExpiredTokens() {
        expiredTokens.clear();

        Mockito.when(tokenService.getExpiredTokens(NOW.toLocalDateTime().plusMinutes(5)))
                .thenReturn(expiredTokens);

        tokenUpdateService.updateExpiredTokens();

        Mockito.verify(apiClient, Mockito.never()).refreshTokens(Mockito.anyString());
        Mockito.verify(tokenService, Mockito.never()).update(Mockito.any());
    }

    private TokenEntity generateTokenEntity(String accessToken, String refreshToken) {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();

        ProviderEntity provider = ProviderEntity.builder()
                .id(UUID.randomUUID())
                .providerName(ProviderName.twitch)
                .providerUserId(PROVIDER_USER_ID)
                .user(user)
                .build();

        return TokenEntity.builder()
                .id(UUID.randomUUID())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .provider(provider)
                .build();
    }
}
