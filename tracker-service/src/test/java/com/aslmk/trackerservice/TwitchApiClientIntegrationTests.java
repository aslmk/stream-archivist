package com.aslmk.trackerservice;

import com.aslmk.trackerservice.client.twitch.TwitchApiClientImpl;
import com.aslmk.trackerservice.client.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.config.AppConfig;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.repository.TwitchAppTokenRepository;
import com.aslmk.trackerservice.service.token.TwitchAppTokenServiceImpl;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

@ActiveProfiles("test")
@SpringBootTest(classes = {TwitchApiClientImpl.class, AppConfig.class, TwitchAppTokenServiceImpl.class})
@WireMockTest(httpPort = 8813)
class TwitchApiClientIntegrationTests {

    @Autowired
    private TwitchApiClientImpl twitchApiClient;

    @MockitoBean
    private TwitchAppTokenRepository tokenRepository;

    private static final String TOKEN_RESPONSE = """
            {
              "access_token": "test_access_token",
              "expires_in": 3600,
              "token_type": "bearer"
            }
            """;

    @BeforeEach
    void setUp() {
        Mockito.when(tokenRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
    }

    private void stubTokenEndpoint() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/oauth2/token"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TOKEN_RESPONSE)));
    }

    private void stubStreamerInfoEndpoint(String username, String responseBody) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/helix/users"))
                .withQueryParam("login", equalTo(username))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    @Test
    void getStreamerInfo_should_returnStreamerInfo_when_twitchRespondsSuccessfully() {
        stubTokenEndpoint();
        stubStreamerInfoEndpoint("test0", """
                {
                  "data": [
                    { "id": "12345", "login": "test0" }
                  ]
                }
                """);

        TwitchStreamerInfo result = twitchApiClient.getStreamerInfo("test0");

        Assertions.assertEquals("12345", result.getId());
    }

    @Test
    void getStreamerInfo_should_throwTwitchApiClientException_when_responseIsEmpty() {
        stubTokenEndpoint();
        stubStreamerInfoEndpoint("unknown", """
                { "data": [] }
                """);

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo("unknown"));
    }

    @Test
    void getStreamerInfo_should_throwTwitchApiClientException_when_responseBodyIsNull() {
        stubTokenEndpoint();
        stubStreamerInfoEndpoint("test0", "null");

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo("test0"));
    }

    @Test
    void getStreamerInfo_should_throwTwitchApiClientException_when_twitchReturnsServerError() {
        stubTokenEndpoint();
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/helix/users"))
                .willReturn(WireMock.aResponse().withStatus(500)));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo("test0"));
    }

    @Test
    void getStreamerInfo_shouldThrow_whenUsernameIsNull() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo(null));
    }

    @Test
    void getStreamerInfo_shouldThrow_whenUsernameIsBlank() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerInfo("  "));
    }

    @Test
    void subscribeToStreamer_should_returnSubscriptionResponse_when_twitchRespondsSuccessfully() {
        stubTokenEndpoint();
        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .willReturn(WireMock.aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
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
                                """)));

        TwitchWebhookSubscriptionResponse result =
                twitchApiClient.subscribeToStreamer("12345", "stream.online");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("e798f6a0-3a22-4629-8be9-4c407e5bb8b4", result.getId().toString());
    }

    @Test
    void subscribeToStreamer_should_throwTwitchApiClientException_when_twitchReturnsError() {
        stubTokenEndpoint();
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
                () -> twitchApiClient.subscribeToStreamer("  ", "stream.online"));
    }

    @Test
    void unsubscribeFromStreamer_should_completeSuccessfully_when_twitchRespondsSuccessfully() {
        stubTokenEndpoint();
        UUID subscriptionId = UUID.randomUUID();

        WireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse().withStatus(204)));

        Assertions.assertDoesNotThrow(
                () -> twitchApiClient.unsubscribeFromStreamer(subscriptionId, "stream.online"));
    }

    @Test
    void unsubscribeFromStreamer_should_throwTwitchApiClientException_when_twitchReturnsError() {
        stubTokenEndpoint();
        UUID subscriptionId = UUID.randomUUID();

        WireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse().withStatus(400)));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.unsubscribeFromStreamer(subscriptionId, "stream.online"));
    }

    @Test
    void unsubscribeFromStreamer_shouldThrow_whenSubscriptionIdIsNull() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.unsubscribeFromStreamer(null, "stream.online"));
    }

    @Test
    void getSubscriptionInfo_should_returnSubscriptionResponse_when_twitchRespondsSuccessfully() {
        stubTokenEndpoint();
        UUID subscriptionId = UUID.fromString("e798f6a0-3a22-4629-8be9-4c407e5bb8b4");

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("subscription_id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "data": [
                                    {
                                      "id": "e798f6a0-3a22-4629-8be9-4c407e5bb8b4",
                                      "type": "stream.online",
                                      "status": "enabled",
                                      "version": "1"
                                    }
                                  ]
                                }
                                """)));

        TwitchWebhookSubscriptionResponse result = twitchApiClient.getSubscriptionInfo(subscriptionId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(subscriptionId.toString(), result.getId().toString());
    }

    @Test
    void getSubscriptionInfo_should_throwTwitchApiClientException_when_twitchReturnsError() {
        stubTokenEndpoint();
        UUID subscriptionId = UUID.randomUUID();

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("subscription_id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse().withStatus(404)));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getSubscriptionInfo(subscriptionId));
    }

    @Test
    void getSubscriptionInfo_should_throwTwitchApiClientException_when_responseIsEmpty() {
        stubTokenEndpoint();
        UUID subscriptionId = UUID.randomUUID();

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("subscription_id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "data": [] }
                                """)));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getSubscriptionInfo(subscriptionId));
    }

    @Test
    void getSubscriptionInfo_should_throwTwitchApiClientException_when_responseBodyIsNull() {
        stubTokenEndpoint();
        UUID subscriptionId = UUID.randomUUID();

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .withQueryParam("subscription_id", equalTo(subscriptionId.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("null")));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getSubscriptionInfo(subscriptionId));
    }
}