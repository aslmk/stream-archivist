package com.aslmk.subscriptionservice;

import com.aslmk.common.dto.TrackStreamerResponse;
import com.aslmk.subscriptionservice.client.TrackerServiceClientImpl;
import com.aslmk.subscriptionservice.config.AppConfig;
import com.aslmk.subscriptionservice.exception.TrackerServiceClientException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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

    private static final String TRACK_STREAMER_ENDPOINT = "/internal/streamers";
    private static final String STREAMER_USERNAME = "test0";
    private static final String PROVIDER_NAME = "twitch";
    private static final String STREAMER_PROFILE_IMAGE_URL = "profile_image_url";

    @Test
    void should_trackStreamerAndReturnIdSuccessfully() {
        UUID streamerId = UUID.randomUUID();

        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TRACK_STREAMER_ENDPOINT))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(String.format("""
                        {
                            "streamerId": "%s",
                            "providerName": "%s",
                            "streamerProfileImageUrl": "%s",
                            "streamerUsername": "%s"
                        }
                        """, streamerId, PROVIDER_NAME, STREAMER_PROFILE_IMAGE_URL, STREAMER_USERNAME))));

        TrackStreamerResponse result = client.trackStreamer(STREAMER_USERNAME, PROVIDER_NAME);

        Assertions.assertEquals(streamerId, result.getStreamerId());
    }

    @Test
    void should_throwException_when_responseBodyIsNull() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TRACK_STREAMER_ENDPOINT))
                .willReturn(WireMock.ok()));

        Assertions.assertThrows(TrackerServiceClientException.class,
                () -> client.trackStreamer(STREAMER_USERNAME, PROVIDER_NAME));
    }

    @Test
    void should_throwException_when_streamerIdIsNull() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(TRACK_STREAMER_ENDPOINT))
                .willReturn(WireMock.okJson("""
                        {
                            "streamerId": null,
                            "providerName": "some_value",
                            "streamerProfileImageUrl": "some_value",
                            "streamerUsername": "some_value"
                        }
                        """)));

        Assertions.assertThrows(TrackerServiceClientException.class,
                () -> client.trackStreamer(STREAMER_USERNAME, PROVIDER_NAME));
    }
}
