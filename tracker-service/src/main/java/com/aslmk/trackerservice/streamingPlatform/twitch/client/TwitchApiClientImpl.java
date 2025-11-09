package com.aslmk.trackerservice.streamingPlatform.twitch.client;

import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
        if (streamerUsername == null || streamerUsername.isBlank()) {
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

            if (apiResponse == null) {
                throw new TwitchApiClientException("Could not get streamer ID from Twitch API: response is null");
            }

            if (apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
                throw new TwitchApiClientException("Could not get streamer ID from Twitch API: response is empty");
            }

            TwitchStreamerInfo streamerInfo = apiResponse.getData().getFirst();
            return streamerInfo.getId();
        } catch (RestClientException e) {
            throw new TwitchApiClientException("Failed to fetch streamer ID for username: " + streamerUsername, e);
        }
    }

    @Override
    public void subscribeToStreamer(String streamerId) {
        if (streamerId == null || streamerId.isBlank()) {
            throw new TwitchApiClientException("Streamer ID cannot be null or blank");
        }

        TwitchSubscribeStreamerRequest request = buildSubscribeRequest(streamerId);

        try {
            restClient.post()
                    .uri(eventSubscribeUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + clientSecret)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
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
