package com.aslmk.trackerservice.streamingPlatform.twitch.client;

import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class TwitchApiClientImpl implements TwitchApiClient {
    @Value("${user.twitch.subscribe.callback-url}")
    private String callbackUrl;
    @Value("${user.twitch.subscribe.secret}")
    private String secret;
    @Value("${user.twitch.client-id}")
    private String clientId;
    @Value("${user.twitch.event.subscribe-url}")
    private String eventSubscribeUrl;
    @Value("${user.twitch.streamer-info-url}")
    private String streamerInfoUrl;
    @Value("${user.twitch.client-secret}")
    private String clientSecret;

    private final RestClient restClient;

    public TwitchApiClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String getStreamerId(String streamerUsername) {
        log.info("Requesting Twitch streamer ID for username='{}'", streamerUsername);

        if (streamerUsername == null || streamerUsername.isBlank()) {
            log.warn("Streamer username validation failed: null or blank");
            throw new TwitchApiClientException("Streamer username cannot be null or blank");
        }

        try {
            TwitchApiResponseDto apiResponse = restClient.get()
                    .uri(streamerInfoUrl + "?login=" + streamerUsername)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + clientSecret)
                    .retrieve()
                    .toEntity(TwitchApiResponseDto.class)
                    .getBody();

            log.debug("Twitch API response for username='{}': {}", streamerUsername, apiResponse);

            if (apiResponse == null) {
                log.error("Twitch API returned NULL response for username='{}'", streamerUsername);
                throw new TwitchApiClientException("Could not get streamer ID from Twitch API: response is null");
            }

            if (apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
                log.warn("Twitch API returned empty data for username='{}'", streamerUsername);
                throw new TwitchApiClientException("Could not get streamer ID from Twitch API: response is empty");
            }

            TwitchStreamerInfo streamerInfo = apiResponse.getData().getFirst();

            log.info("Twitch streamer ID retrieved: username='{}', id='{}'",
                    streamerUsername, streamerInfo.getId());

            return streamerInfo.getId();
        } catch (RestClientException e) {
            log.error("Failed to fetch Twitch streamer ID for username='{}'", streamerUsername, e);
            throw new TwitchApiClientException("Failed to fetch streamer ID for username: " + streamerUsername, e);
        }
    }

    @Override
    public void subscribeToStreamer(String streamerId) {

        log.info("Subscribing to Twitch stream.online for streamerId='{}'", streamerId);

        if (streamerId == null || streamerId.isBlank()) {
            log.warn("Streamer ID validation failed: null or blank");
            throw new TwitchApiClientException("Streamer ID cannot be null or blank");
        }

        TwitchSubscribeStreamerRequest request = buildSubscribeRequest(streamerId);
        log.debug("Built Twitch subscription payload: {}", request);

        try {
            restClient.post()
                    .uri(eventSubscribeUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + clientSecret)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Twitch EventSub subscription created successfully for streamerId='{}'", streamerId);
        } catch (RestClientException e) {
            log.error("Failed to subscribe to Twitch EventSub for streamerId='{}'", streamerId, e);
            throw new TwitchApiClientException("Failed to subscribe to streamer with ID: " + streamerId, e);
        }
    }

    private TwitchSubscribeStreamerRequest buildSubscribeRequest(String streamerId) {
        return TwitchSubscribeStreamerRequest.builder()
                .type("stream.online")
                .version("1")
                .condition(TwitchCondition.builder()
                        .broadcasterUserId(streamerId)
                        .build()
                )
                .transport(TwitchTransport.builder()
                        .method("webhook")
                        .callback(callbackUrl)
                        .secret(secret)
                        .build()
                )
                .build();
    }
}
