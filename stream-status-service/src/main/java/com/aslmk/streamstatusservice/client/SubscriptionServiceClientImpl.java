package com.aslmk.streamstatusservice.client;

import com.aslmk.common.dto.EntityIdResolveResponse;
import com.aslmk.common.dto.TrackedStreamersResponse;
import com.aslmk.streamstatusservice.exception.SubscriptionServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SubscriptionServiceClientImpl implements SubscriptionServiceClient {

    @Value("${user.subscription-service.base-url}")
    private String subscriptionServiceBaseUrl;

    private static final String TRACKED_STREAMERS_ENDPOINT = "/internal/users/%s/streamers";

    private final RestClient restClient;

    public SubscriptionServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<EntityIdResolveResponse> getTrackedStreamers(UUID userId) {
        log.info("Fetching tracked streamers for user='{}'", userId);
        try {
            TrackedStreamersResponse response = restClient.get()
                    .uri(subscriptionServiceBaseUrl + String.format(TRACKED_STREAMERS_ENDPOINT, userId))
                    .retrieve()
                    .toEntity(TrackedStreamersResponse.class)
                    .getBody();

            if (response == null || response.getStreamers() == null) {
                log.error("Failed to get tracked streamers for user='{}': response is null", userId);
                throw new SubscriptionServiceClientException(
                        String.format("Could not get tracked streamers for user='%s': response is null", userId));
            }

            log.info("Successfully get tracked streamers for user='{}'", userId);
            return response.getStreamers();
        } catch (RestClientException e) {
            throw new SubscriptionServiceClientException(
                    String.format("Failed to get tracked streamers for user='%s'", userId), e);
        }
    }
}
