package com.aslmk.subscriptionservice;

import com.aslmk.subscriptionservice.client.AuthServiceClientImpl;
import com.aslmk.subscriptionservice.config.AppConfig;
import com.aslmk.subscriptionservice.exception.AuthServiceClientException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppConfig.class, AuthServiceClientImpl.class})
@WireMockTest(httpPort = 8813)
public class AuthServiceClientIntegrationTests {

    @Autowired
    private RestClient restClient;

    @Autowired
    private AuthServiceClientImpl client;

    private static final String RESOLVE_USER_ID_ENDPOINT = "/internal/users/resolve";
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";


    @Test
    void should_resolveUserIdSuccessfully() {
        UUID userId = UUID.randomUUID();

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(RESOLVE_USER_ID_ENDPOINT))
                .withQueryParam("providerUserId", WireMock.equalTo(PROVIDER_USER_ID))
                .withQueryParam("providerName", WireMock.equalTo(PROVIDER_NAME))
                .willReturn(WireMock.okJson(
                        String.format("""
                        {
                            "userId": "%s"
                        }
                        """, userId))));

        UUID result = client.resolveUserId(PROVIDER_USER_ID, PROVIDER_NAME);

        Assertions.assertEquals(userId, result);
    }

    @Test
    void should_throwException_when_responseBodyIsNull() {
        WireMock.stubFor(WireMock.get(WireMock.urlMatching(RESOLVE_USER_ID_ENDPOINT))
                .willReturn(WireMock.ok()));

        Assertions.assertThrows(AuthServiceClientException.class,
                () -> client.resolveUserId(PROVIDER_USER_ID, PROVIDER_NAME));
    }

    @Test
    void should_throwException_when_userIdIsNull() {
        WireMock.stubFor(WireMock.get(WireMock.urlMatching(RESOLVE_USER_ID_ENDPOINT))
                .willReturn(WireMock.okJson("""
                        {
                            "userId": null
                        }
                        """)));

        Assertions.assertThrows(AuthServiceClientException.class,
                () -> client.resolveUserId(PROVIDER_USER_ID, PROVIDER_NAME));
    }

}
