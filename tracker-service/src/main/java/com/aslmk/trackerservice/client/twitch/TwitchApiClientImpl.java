package com.aslmk.trackerservice.client.twitch;

import com.aslmk.trackerservice.domain.TwitchAppTokenEntity;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.service.token.TwitchAppTokenService;
import com.aslmk.trackerservice.client.twitch.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public TwitchStreamerInfo getStreamerInfo(String streamerUsername) {
        log.info("Requesting Twitch streamer info for username='{}'", streamerUsername);

        if (streamerUsername == null || streamerUsername.isBlank()) {
            log.warn("Streamer username validation failed: null or blank");
            throw new TwitchApiClientException("Streamer username cannot be null or blank");
        }

        String appAccessToken = getAppToken();

        try {
            TwitchApiResponseDto<TwitchStreamerInfo> apiResponse = restClient.get()
                    .uri(streamerInfoUrl + "?login=" + streamerUsername)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + appAccessToken)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<TwitchApiResponseDto<TwitchStreamerInfo>>() {})
                    .getBody();

            log.debug("Twitch API response for username='{}': {}", streamerUsername, apiResponse);
            List<TwitchStreamerInfo> data = extractData(apiResponse);
            validateData(data);

            TwitchStreamerInfo streamerInfo = data.getFirst();

            log.info("Twitch streamer info retrieved: username='{}', id='{}'",
                    streamerUsername, streamerInfo.getId());

            return streamerInfo;
        } catch (RestClientException e) {
            log.error("Failed to fetch Twitch streamer info for username='{}'", streamerUsername, e);
            throw new TwitchApiClientException("Failed to fetch streamer info for username: " + streamerUsername, e);
        }
    }

    @Override
    public TwitchWebhookSubscriptionResponse subscribeToStreamer(String streamerId, String eventType) {
        log.info("Subscribing to Twitch event '{}': streamerId='{}'", eventType, streamerId);

        if (streamerId == null || streamerId.isBlank()) {
            throw new TwitchApiClientException("Streamer ID cannot be null or blank");
        }

        String appAccessToken = getAppToken();

        return subscribeToEvent(appAccessToken, streamerId, eventType);
    }

    @Override
    public void unsubscribeFromStreamer(UUID subscriptionId, String eventType) {
        log.info("Unsubscribing from Twitch event '{}': subscriptionId='{}'", eventType, subscriptionId);

        if (subscriptionId == null) {
            throw new TwitchApiClientException("Subscription ID cannot be null or blank");
        }

        String appAccessToken = getAppToken();

        unsubscribeFromEvent(appAccessToken, subscriptionId, eventType);
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

    private TwitchSubscribeStreamerRequest buildSubscribeRequest(String streamerId, String eventType) {
        return TwitchSubscribeStreamerRequest.builder()
                .type(eventType)
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

    private TwitchWebhookSubscriptionResponse subscribeToEvent(String appAccessToken,
                                                               String streamerId,
                                                               String eventType) {
        try {
            TwitchSubscribeStreamerRequest request = buildSubscribeRequest(streamerId, eventType);

            TwitchApiResponseDto<TwitchWebhookSubscriptionResponse> apiResponse = restClient.post()
                    .uri(eventSubscribeUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + appAccessToken)
                    .body(request)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<TwitchApiResponseDto<TwitchWebhookSubscriptionResponse>>() {})
                    .getBody();

            log.debug("Twitch API response for streamerId='{}': {}", streamerId, apiResponse);
            List<TwitchWebhookSubscriptionResponse> data = extractData(apiResponse);
            validateData(data);

            log.info("Subscribed to '{}' for streamerId='{}'", eventType, streamerId);

            // The list is in ascending order by creation date (oldest subscription first)
            return data.getLast();
        } catch (RestClientException e) {
            throw new TwitchApiClientException(
                    String.format("Failed to subscribe to '%s' for streamerId='%s'",
                            eventType, streamerId), e);
        }
    }

    private void unsubscribeFromEvent(String appAccessToken, UUID subscriptionId, String eventType) {
        try {
            String unsubscribeUrl = eventSubscribeUrl+"?id="+ subscriptionId;

            restClient.delete()
                    .uri(unsubscribeUrl)
                    .header("Client-Id", clientId)
                    .header("Authorization", "Bearer " + appAccessToken)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Unsubscribed from '{}': subscriptionId='{}'", eventType, subscriptionId);
        } catch (RestClientException e) {
            throw new TwitchApiClientException(
                    String.format("Failed to unsubscribe from '%s' event: subscriptionId='%s'",
                            eventType, subscriptionId), e);
        }
    }

    private <T> void validateData(List<T> data) {
        if (data == null || data.isEmpty()) {
            log.warn("Twitch API returned empty data");
            throw new TwitchApiClientException("Could not get data from Twitch API: response is empty");
        }
    }

    private <T> List<T> extractData(TwitchApiResponseDto<T> apiResponse) {
        if (apiResponse == null) {
            log.error("Twitch API returned NULL response");
            throw new TwitchApiClientException("Could not get data from Twitch API: response is null");
        }
        return apiResponse.getData();
    }
}
