package com.aslmk.trackerservice.streamingPlatform.twitch.client;

import com.aslmk.trackerservice.entity.TwitchAppTokenEntity;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.service.TwitchAppTokenService;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

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
    @Value("${user.twitch.token-url}")
    private String tokenUrl;

    private final RestClient restClient;
    private final TwitchAppTokenService service;
    private final Clock clock;

    public TwitchApiClientImpl(RestClient restClient, TwitchAppTokenService service, Clock clock) {
        this.restClient = restClient;
        this.service = service;
        this.clock = clock;
    }

    @Override
    public String getStreamerId(String streamerUsername) {
        log.info("Requesting Twitch streamer ID for username='{}'", streamerUsername);

        if (streamerUsername == null || streamerUsername.isBlank()) {
            log.warn("Streamer username validation failed: null or blank");
            throw new TwitchApiClientException("Streamer username cannot be null or blank");
        }

        String appAccessToken = getAppToken();

        try {
            TwitchApiResponseDto apiResponse = restClient.get()
                    .uri(streamerInfoUrl + "?login=" + streamerUsername)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + appAccessToken)
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

        String appAccessToken = getAppToken();

        try {
            restClient.post()
                    .uri(eventSubscribeUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + appAccessToken)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Twitch EventSub subscription created successfully for streamerId='{}'", streamerId);
        } catch (RestClientException e) {
            log.error("Failed to subscribe to Twitch EventSub for streamerId='{}'", streamerId, e);
            throw new TwitchApiClientException("Failed to subscribe to streamer with ID: " + streamerId, e);
        }
    }

    private TwitchAppAccessToken getAppAccessToken() {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("grant_type", "client_credentials");

            TwitchAppAccessToken token = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toEntity(TwitchAppAccessToken.class)
                    .getBody();

            log.info("App access token retrieved");
            return token;
        } catch (RestClientException e) {
            log.error("Failed to fetch to app access token from Twitch", e);
            throw new TwitchApiClientException("Failed to fetch to app access token from Twitch", e);
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

    private String getAppToken() {
        Optional<TwitchAppTokenEntity> dbAppToken = service.getAppAccessToken();

        if (dbAppToken.isEmpty()) {
            log.info("No App Access Token found in database — fetching new one from Twitch");
            TwitchAppAccessToken appToken = getAppAccessToken();
            service.save(appToken);
            log.info("New App Access Token fetched and saved successfully, expiresIn = {}", appToken.getExpiresIn());
            return appToken.getAccessToken();
        }

        TwitchAppTokenEntity dbToken = dbAppToken.get();

        if (dbToken.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            log.warn("App Access Token has expired (expiresAt = {}) — refreshing", dbToken.getExpiresAt());
            TwitchAppAccessToken appToken = getAppAccessToken();
            service.update(dbToken, appToken);
            log.info("App Access Token refreshed successfully, new expiresIn = {} seconds", appToken.getExpiresIn());
            return appToken.getAccessToken();
        }

        log.debug("Using existing valid App Access Token, expiresAt = {}", dbToken.getExpiresAt());
        return dbToken.getAccessToken();
    }
}
