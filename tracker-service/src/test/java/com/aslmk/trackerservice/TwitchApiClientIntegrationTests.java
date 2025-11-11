package com.aslmk.trackerservice;

import com.aslmk.trackerservice.config.AppConfig;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.streamingPlatform.twitch.client.TwitchApiClientImpl;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

@ActiveProfiles("test")
@SpringBootTest(classes = {TwitchApiClientImpl.class, AppConfig.class})
@WireMockTest(httpPort = 8813)
class TwitchApiClientIntegrationTests {

    @Autowired
    private RestClient restClient;

    @Autowired
    private TwitchApiClientImpl twitchApiClient;

    @Test
    void getStreamerId_should_returnCorrectId_when_twitchRespondsSuccessfully() {
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

        String result = twitchApiClient.getStreamerId("test0");

        Assertions.assertEquals("12345", result);
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
                () -> twitchApiClient.getStreamerId("unknown"));
    }

    @Test
    void getStreamerId_shouldThrow_whenUsernameIsNull() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerId(null));
    }

    @Test
    void getStreamerId_shouldThrow_whenUsernameIsBlank() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.getStreamerId(" "));
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
                () -> twitchApiClient.getStreamerId("test0"));
    }

    @Test
    void subscribeToStreamer_should_postRequestSuccessfully() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .willReturn(WireMock.aResponse().withStatus(202)));

        Assertions.assertDoesNotThrow(() -> twitchApiClient.subscribeToStreamer("12345"));
    }

    @Test
    void subscribeToStreamer_should_throwTwitchApiClientException_when_responseIsError() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventsub/subscriptions"))
                .willReturn(WireMock.aResponse().withStatus(400)));

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.subscribeToStreamer("12345"));
    }

    @Test
    void subscribeToStreamer_shouldThrow_whenStreamerIdIsNull() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.subscribeToStreamer(null));
    }

    @Test
    void subscribeToStreamer_shouldThrow_whenStreamerIdIsBlank() {
        Assertions.assertThrows(TwitchApiClientException.class,
                () -> twitchApiClient.subscribeToStreamer(" "));
    }
}
