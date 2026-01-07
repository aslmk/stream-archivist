package com.aslmk.subscriptionservice.client;

import com.aslmk.common.dto.EntityIdResolveResponse;
import com.aslmk.subscriptionservice.exception.TrackerServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
@Slf4j
public class TrackerServiceClientImpl implements TrackerServiceClient {

    private final RestClient restClient;

    @Value("${user.tracker-service.url}")
    private String trackerServiceUrl;

    public TrackerServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public UUID resolveStreamerId(String providerUserId, String providerName) {
        log.debug("Resolving streamer: providerUserId='{}', provider='{}'", providerUserId, providerName);
        try {
            EntityIdResolveResponse response = restClient.get()
                    .uri(UriComponentsBuilder.fromUriString(trackerServiceUrl)
                            .queryParam("providerUserId", providerUserId)
                            .queryParam("providerName", providerName)
                            .build()
                            .toUri())
                    .retrieve()
                    .toEntity(EntityIdResolveResponse.class)
                    .getBody();

            if (response == null || response.getEntityId() == null) {
                log.error("Failed to resolve streamer (providerUserId='{}', provider='{}'): tracker-service returned invalid response", providerUserId, providerName);
                throw new TrackerServiceClientException("Tracker-service returned invalid response");
            }

            log.debug("Streamer resolved: providerUserId='{}', provider='{}'", providerUserId, providerName);
            return response.getEntityId();
        } catch (RestClientException e) {
            throw new TrackerServiceClientException("Failed to resolve streamer via tracker-service", e);
        }
    }
}
