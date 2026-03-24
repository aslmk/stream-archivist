package com.aslmk.trackerservice;

import com.aslmk.trackerservice.config.AppConfig;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.repository.TwitchAppTokenRepository;
import com.aslmk.trackerservice.service.token.TwitchAppTokenServiceImpl;
import com.aslmk.trackerservice.client.twitch.TwitchApiClientImpl;
import com.aslmk.trackerservice.client.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

@ActiveProfiles("test")
@SpringBootTest(classes = {TwitchApiClientImpl.class, AppConfig.class, TwitchAppTokenServiceImpl.class})
@WireMockTest(httpPort = 8813)
class TwitchApiClientIntegrationTests {

    @Autowired
    private RestClient restClient;

    @Autowired
    private TwitchApiClientImpl twitchApiClient;

    @MockitoBean
    private TwitchAppTokenRepository tokenRepository;

    @Test
    void getStreamerId_should_returnCorrectId_when_twitchRespondsSuccessfully() {
        String tokenResponse = """
            {
              "access_token": "test_access_token",
              "expires_in": 3600,
              "token_type": "bearer"
            }
            """;

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/oauth2/token"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tokenResponse)
                )
        );

        String jsonResponse = """
                {
                  "data": [
                    {
                      "id": "12345",
                      "login": "test0"
                    }
                  ]
                }
                """;

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/helix/users"))
                .withQueryParam("login", equalTo("test0"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)
                )
        );

        TwitchStreamerInfo result = twitchApiClient.getStreamerInfo("test0");

        Assertions.assertEquals("12345", result.getId());
    }

    @Test
    void getStreamerId_should_throwTwitchApiClientException_when_responseEmpty() {
        String jsonResponse = """
                { "data": [] }
                """;

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/helix/users"))
                .withQueryParam("login", equalTo("unknown"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)
                )
        );

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo("unknown"));
    }

    @Test
    void getStreamerId_shouldThrow_whenUsernameIsNull() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo(null));
    }

    @Test
    void getStreamerId_shouldThrow_whenUsernameIsBlank() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo(" "));
    }

    @Test
    void getStreamerId_shouldThrow_whenResponseIsNullBody() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/helix/users"))
                .withQueryParam("login", equalTo("test0"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("null")));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo("test0"));
    }

    @Test
    void subscribeToStreamer_should_returnSubscriptionResponse_when_twitchRespondsSuccessfully() {
        String tokenResponse = """
            {
              "access_token": "test_access_token",
              "expires_in": 3600,
              "token_type": "bearer"
            }
            """;

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/oauth2/token"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tokenResponse)
                )
        );

        String subscribeResponse = """
                {
                  "data": [
                    {
                      "id": "e798f6a0-3a22-4629-8be9-4c407e5bb8b4",
                      "type": "stream.online",
                      "status": "webhook_callback_verification_pending",
                      "version": "1"
                    }
                  ]
                }
                """;

        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .willReturn(WireMock.aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody(subscribeResponse)
                )
        );

        TwitchWebhookSubscriptionResponse result =
                twitchApiClient.subscribeToStreamer("12345", "stream.online");

        Assertions.assertNotNull(result);
    }

    @Test
    void subscribeToStreamer_should_throwTwitchApiClientException_when_responseIsError() {
        String tokenResponse = """
            {
              "access_token": "test_access_token",
              "expires_in": 3600,
              "token_type": "bearer"
            }
            """;

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/oauth2/token"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tokenResponse)
                )
        );

        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .willReturn(WireMock.aResponse().withStatus(400)));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.subscribeToStreamer("12345", "stream.online"));
    }

    @Test
    void subscribeToStreamer_shouldThrow_whenStreamerIdIsNull() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.subscribeToStreamer(null, "stream.online"));
    }

    @Test
    void subscribeToStreamer_shouldThrow_whenStreamerIdIsBlank() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.subscribeToStreamer(" ", "stream.online"));
    }

    @Test
    void unsubscribeFromStreamer_should_sendRequestSuccessfully() {
        String tokenResponse = """
            {
              "access_token": "test_access_token",
              "expires_in": 3600,
              "token_type": "bearer"
            }
            """;

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/oauth2/token"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tokenResponse)
                )
        );

        UUID subscriptionId = UUID.randomUUID();

        WireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse().withStatus(204))
        );

        Assertions.assertDoesNotThrow(
                () -> twitchApiClient.unsubscribeFromStreamer(subscriptionId, "stream.online"));
    }

    @Test
    void unsubscribeFromStreamer_should_throwTwitchApiClientException_when_responseIsError() {
        String tokenResponse = """
            {
              "access_token": "test_access_token",
              "expires_in": 3600,
              "token_type": "bearer"
            }
            """;

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/oauth2/token"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tokenResponse)
                )
        );

        UUID subscriptionId = UUID.randomUUID();

        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse().withStatus(400))
        );

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.unsubscribeFromStreamer(subscriptionId, "stream.online"));
    }

    @Test
    void unsubscribeFromStreamer_shouldThrow_whenSubscriptionIdIsNull() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.unsubscribeFromStreamer(null, "stream.online"));
    }
}