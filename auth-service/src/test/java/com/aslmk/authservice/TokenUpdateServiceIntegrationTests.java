package com.aslmk.authservice;

import com.aslmk.authservice.client.twitch.TwitchApiClient;
import com.aslmk.authservice.client.twitch.TwitchTokenRefreshResponse;
import com.aslmk.authservice.entity.ProviderEntity;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.entity.TokenEntity;
import com.aslmk.authservice.entity.UserEntity;
import com.aslmk.authservice.exception.TwitchApiClientException;
import com.aslmk.authservice.repository.ProviderRepository;
import com.aslmk.authservice.repository.TokenRepository;
import com.aslmk.authservice.repository.UserRepository;
import com.aslmk.authservice.service.TokenService;
import com.aslmk.authservice.service.TokenUpdateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
@Transactional
public class TokenUpdateServiceIntegrationTests {

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2025, 10, 22,
            16, 0, 0, 0,
            ZoneId.of("UTC")
    );

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        public Clock clock() {
            return Clock.fixed(NOW.toInstant(), NOW.getZone());
        }
    }

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Autowired
    private TokenUpdateService tokenUpdateService;
    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private ProviderRepository providerRepository;
    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TwitchApiClient apiClient;

    private static final String FIRST_ACCESS_TOKEN_VALUE = "first_access_token_value";
    private static final String FIRST_REFRESH_TOKEN_VALUE = "first_refresh_token_value";
    private static final String SECOND_ACCESS_TOKEN_VALUE = "second_access_token_value";
    private static final String SECOND_REFRESH_TOKEN_VALUE = "second_refresh_token_value";
    private static final String FIRST_PROVIDER_USER_ID = "123456789";
    private static final String SECOND_PROVIDER_USER_ID = "8104102108";

    private TwitchTokenRefreshResponse twitchResponse;

    @BeforeEach
    void setUp() {
        twitchResponse = new TwitchTokenRefreshResponse();
        twitchResponse.setAccessToken("access_token_from_twitch");
        twitchResponse.setRefreshToken("refresh_token_from_twitch");
        twitchResponse.setScopes(List.of("scope1", "scope2"));
        twitchResponse.setExpiresIn(3600);
    }

    @Test
    void should_updateExpiredToken() {
        UserEntity newUser = generateNewUser();
        ProviderEntity newProvider = generateNewProvider(FIRST_PROVIDER_USER_ID, newUser);
        TokenEntity expiredToken = generateExpiredToken(FIRST_ACCESS_TOKEN_VALUE, FIRST_REFRESH_TOKEN_VALUE, newProvider);

        Mockito.when(apiClient.refreshTokens(Mockito.anyString())).thenReturn(twitchResponse);

        tokenUpdateService.updateExpiredTokens();

        TokenEntity dbToken = tokenRepository.findById(expiredToken.getId()).orElseThrow();

        Assertions.assertNotEquals(FIRST_ACCESS_TOKEN_VALUE, dbToken.getAccessToken());
        Assertions.assertNotEquals(FIRST_REFRESH_TOKEN_VALUE, dbToken.getRefreshToken());
        Assertions.assertEquals(NOW.plusSeconds(3600).toLocalDateTime(), dbToken.getExpiresAt());
    }

    @Test
    void should_updateOneOutOfTwoExpiredTokens() {
        UserEntity newUser = generateNewUser();
        ProviderEntity newProvider = generateNewProvider(FIRST_PROVIDER_USER_ID, newUser);
        TokenEntity expiredToken = generateExpiredToken(FIRST_ACCESS_TOKEN_VALUE, FIRST_REFRESH_TOKEN_VALUE, newProvider);

        UserEntity newUser2 = generateNewUser();
        ProviderEntity newProvider2 = generateNewProvider(SECOND_PROVIDER_USER_ID, newUser2);
        TokenEntity expiredToken2 = generateExpiredToken(SECOND_ACCESS_TOKEN_VALUE, SECOND_REFRESH_TOKEN_VALUE, newProvider2);

        Mockito.when(apiClient.refreshTokens(Mockito.anyString()))
                .thenReturn(twitchResponse)
                .thenThrow(TwitchApiClientException.class);

        tokenUpdateService.updateExpiredTokens();

        Assertions.assertEquals(2, tokenRepository.count());

        TokenEntity dbToken = tokenRepository.findById(expiredToken.getId()).orElseThrow();
        Assertions.assertNotEquals(FIRST_ACCESS_TOKEN_VALUE, dbToken.getAccessToken());
        Assertions.assertNotEquals(FIRST_REFRESH_TOKEN_VALUE, dbToken.getRefreshToken());
        Assertions.assertEquals(NOW.plusSeconds(3600).toLocalDateTime(), dbToken.getExpiresAt());

        TokenEntity dbToken2 = tokenRepository.findById(expiredToken2.getId()).orElseThrow();
        Assertions.assertEquals(SECOND_ACCESS_TOKEN_VALUE, dbToken2.getAccessToken());
        Assertions.assertEquals(SECOND_REFRESH_TOKEN_VALUE, dbToken2.getRefreshToken());
        Assertions.assertEquals(expiredToken2.getExpiresAt(), dbToken2.getExpiresAt());
    }

    private TokenEntity generateExpiredToken(String accessToken, String refreshToken, ProviderEntity provider) {
        TokenEntity token = TokenEntity.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .provider(provider)
                .expiresAt(NOW.toLocalDateTime())
                .build();

        return tokenRepository.save(token);
    }
    private UserEntity generateNewUser() {
        return userRepository.save(UserEntity.builder().build());
    }
    private ProviderEntity generateNewProvider(String providerUserId, UserEntity user) {
        ProviderEntity provider = ProviderEntity.builder()
                .providerName(ProviderName.twitch)
                .providerUserId(providerUserId)
                .user(user)
                .build();

        return providerRepository.save(provider);
    }
}