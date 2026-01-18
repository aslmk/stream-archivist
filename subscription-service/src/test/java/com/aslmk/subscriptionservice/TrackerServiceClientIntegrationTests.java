package com.aslmk.subscriptionservice;

import com.aslmk.subscriptionservice.client.TrackerServiceClientImpl;
import com.aslmk.subscriptionservice.config.AppConfig;
import com.aslmk.subscriptionservice.exception.TrackerServiceClientException;
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
@SpringBootTest(classes = {AppConfig.class, TrackerServiceClientImpl.class})
@WireMockTest(httpPort = 8813)
public class TrackerServiceClientIntegrationTests {

    @Autowired
    private RestClient restClient;

    @Autowired
    private TrackerServiceClientImpl client;

    private static final String RESOLVE_STREAMER_ID_ENDPOINT = "/internal/streamers/resolve";
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";

    @Test
    void should_resolveStreamerIdSuccessfully() {
        UUID streamerId = UUID.randomUUID();

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(RESOLVE_STREAMER_ID_ENDPOINT))
                .withQueryParam("providerUserId", WireMock.equalTo(PROVIDER_USER_ID))
                .withQueryParam("providerName", WireMock.equalTo(PROVIDER_NAME))
                .willReturn(WireMock.okJson(
                        String.format("""
                        {
                            "entityId": "%s"
                        }
                        """, streamerId))));

        UUID result = client.resolveStreamerId(PROVIDER_USER_ID, PROVIDER_NAME);

        Assertions.assertEquals(streamerId, result);
    }

    @Test
    void should_throwException_when_responseBodyIsNull() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(RESOLVE_STREAMER_ID_ENDPOINT))
                .willReturn(WireMock.ok()));

        Assertions.assertThrows(TrackerServiceClientException.class,
                () -> client.resolveStreamerId(PROVIDER_USER_ID, PROVIDER_NAME));
    }

    @Test
    void should_throwException_when_userIdIsNull() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(RESOLVE_STREAMER_ID_ENDPOINT))
                .willReturn(WireMock.okJson("""
                        {
                            "entityId": null
                        }
                        """)));

        Assertions.assertThrows(TrackerServiceClientException.class,
                () -> client.resolveStreamerId(PROVIDER_USER_ID, PROVIDER_NAME));
    }
}
