package com.aslmk.authservice;

import com.aslmk.authservice.entity.*;
import com.aslmk.authservice.repository.AccountRepository;
import com.aslmk.authservice.repository.ProviderRepository;
import com.aslmk.authservice.repository.TokenRepository;
import com.aslmk.authservice.repository.UserRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WireMockTest(httpPort = 8811)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
public class OAuthFlowIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test-db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ProviderRepository providerRepository;
    @Autowired
    private TokenRepository tokenRepository;

    private static final String REDIRECT_URI =  "/login/oauth2/code/twitch";
    private static final String TOKEN_URI = "/oauth2/token";
    private static final String USER_INFO_URI = "/oauth2/userinfo";
    private static final String SPRING_SECURITY_OAUTH2_ENDPOINT = "/oauth2/authorization/twitch";

    private static final String ACCESS_TOKEN = "o4ooirh3qorhw0fhads0fhs0dfhasd0f";
    private static final String REFRESH_TOKEN = "jaifhjds0fahs8e0fh302hf3p2ifhdspfha0dsfh302";

    private static final Integer RAW_EXPIRES_AT = 1760599546;
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(
            2025, 8, 15,
            14, 12, 26);

    private static final String PROVIDER_USER_ID = "24928942342";
    private static final String USERNAME = "test-user";
    private static final String CLIENT_ID = "mock-client";

    @BeforeEach
    void setUp() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_URI))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("{\n" +
                                "    \"access_token\": \""+ ACCESS_TOKEN + "\", \n" +
                                "    \"expires_in\": " + RAW_EXPIRES_AT + ",\n" +
                                "    \"refresh_token\": \"" + REFRESH_TOKEN + "\",\n" +
                                "    \"scope\": [\"user:read:email\"],\n" +
                                "    \"token_type\": \"bearer\"\n" +
                                "}")
                )
        );
    }

    @Test
    void should_saveDataIntoDatabase() throws Exception {
        performOAuthFlow(mockMvc, buildUserInfoResponse(PROVIDER_USER_ID, USERNAME));

        Assertions.assertEquals(1, userRepository.count());
        Assertions.assertEquals(1, accountRepository.count());
        Assertions.assertEquals(1, providerRepository.count());
        Assertions.assertEquals(1, tokenRepository.count());
    }

    @Test
    void should_refreshTwitchTokens_when_accountAlreadyExists() throws Exception {
        UserEntity user = UserEntity.builder().build();
        UserEntity savedUser = userRepository.save(user);
        ProviderEntity savedProvider = createProvider(PROVIDER_USER_ID, ProviderName.twitch, savedUser);
        TokenEntity savedToken = createToken("expiredAccessToken", "expiredRefreshToken", EXPIRES_AT, savedProvider);
        AccountEntity savedAccount = createAccount(PROVIDER_USER_ID, ProviderName.twitch, savedUser, savedProvider);

        savedProvider.setToken(savedToken);

        performOAuthFlow(mockMvc, buildUserInfoResponse(PROVIDER_USER_ID, USERNAME));

        Assertions.assertEquals(1, userRepository.count());
        Assertions.assertEquals(1, accountRepository.count());
        Assertions.assertEquals(1, providerRepository.count());
        Assertions.assertEquals(1, tokenRepository.count());

        Assertions.assertTrue(userRepository.existsById(savedUser.getId()));
        Assertions.assertTrue(accountRepository.existsById(savedAccount.getId()));
        Assertions.assertTrue(providerRepository.existsById(savedProvider.getId()));
        Assertions.assertTrue(tokenRepository.existsById(savedToken.getId()));

        TokenEntity refreshedToken = tokenRepository.findById(savedToken.getId()).orElseThrow();
        Assertions.assertNotEquals("expiredAccessToken", refreshedToken.getAccessToken());
        Assertions.assertNotEquals("expiredRefreshToken", refreshedToken.getRefreshToken());
    }

    @Test
    void should_createNewUser_when_differentProviderUserId() throws Exception {
        performOAuthFlow(mockMvc, buildUserInfoResponse(PROVIDER_USER_ID, USERNAME));

        Assertions.assertEquals(1, userRepository.count());
        Assertions.assertEquals(1, accountRepository.count());
        Assertions.assertEquals(1, providerRepository.count());
        Assertions.assertEquals(1, tokenRepository.count());

        String newProviderUserId = "3081034810";
        String newUsername = "testUser2";

        performOAuthFlow(mockMvc, buildUserInfoResponse(newProviderUserId, newUsername));

        Assertions.assertEquals(2, userRepository.count());
        Assertions.assertEquals(2, accountRepository.count());
        Assertions.assertEquals(2, providerRepository.count());
        Assertions.assertEquals(2, tokenRepository.count());

        Iterable<UserEntity> users = userRepository.findAll();
        List<UserEntity> usersList = new ArrayList<>();
        for (UserEntity user : users) {
            usersList.add(user);
        }

        Assertions.assertNotEquals(usersList.get(0), usersList.get(1));

        Iterable<ProviderEntity> providers = providerRepository.findAll();
        List<ProviderEntity> providersList = new ArrayList<>();
        for (ProviderEntity provider : providers) {
            providersList.add(provider);
        }

        Assertions.assertTrue(providersList.stream()
                .anyMatch(p -> p.getProviderUserId().equals(PROVIDER_USER_ID)));
        Assertions.assertTrue(providersList.stream()
                .anyMatch(p -> p.getProviderUserId().equals(newProviderUserId)));
    }

    private void performOAuthFlow(MockMvc mockMvc, String userInfoResponse) throws Exception {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(USER_INFO_URI))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(userInfoResponse)
                )
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(SPRING_SECURITY_OAUTH2_ENDPOINT))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        Assertions.assertNotNull(session);

        mockMvc.perform(MockMvcRequestBuilders.get(REDIRECT_URI)
                        .param("code", "mock-code")
                        .param("state", extractState(result))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"));
    }

    private String buildUserInfoResponse(String providerUserId, String username) {
        return "{\n" +
                "    \"aud\": \"" + CLIENT_ID + "\",\n" +
                "    \"exp\": " + RAW_EXPIRES_AT + ",\n" +
                "    \"iat\": " + RAW_EXPIRES_AT + ",\n" +
                "    \"iss\": \"https://id.twitch.tv/oauth2\",\n" +
                "    \"sub\": \""+ providerUserId + "\",\n" +
                "    \"azp\": \""+ CLIENT_ID + "\",\n" +
                "    \"preferred_username\": \"" + username + "\"\n" +
                "}";
    }

    private AccountEntity createAccount(String providerUserId,
                               ProviderName providerName,
                               UserEntity user,
                               ProviderEntity provider) {
        AccountEntity account = AccountEntity.builder()
                .providerUserId(providerUserId)
                .providerName(providerName)
                .provider(provider)
                .user(user)
                .build();

        return accountRepository.save(account);
    }

    private TokenEntity createToken(String accessToken,
                             String refreshToken,
                             LocalDateTime expiresAt,
                             ProviderEntity provider) {

        TokenEntity token = TokenEntity.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .provider(provider)
                .expiresAt(expiresAt)
                .build();

        return tokenRepository.save(token);
    }

    private ProviderEntity createProvider(String providerUserId,
                                          ProviderName providerName,
                                          UserEntity user) {
        ProviderEntity provider = ProviderEntity.builder()
                .user(user)
                .providerUserId(providerUserId)
                .providerName(providerName)
                .build();
        return providerRepository.save(provider);
    }


    private String extractState(MvcResult result) {
        String redirectUrl = result.getResponse().getRedirectedUrl();
        Assertions.assertNotNull(redirectUrl);

        String encodedState = UriComponentsBuilder.fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .getFirst("state");

        Assertions.assertNotNull(encodedState);

        return UriUtils.decode(encodedState, StandardCharsets.UTF_8);
    }
}
