package com.aslmk.subscriptionservice.client;

import com.aslmk.subscriptionservice.dto.TrackStreamerResponse;
import com.aslmk.subscriptionservice.dto.TrackingRequestDto;
import com.aslmk.subscriptionservice.exception.TrackerServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
    public TrackStreamerResponse trackStreamer(String streamerUsername, String providerName) {
        log.debug("Tracking streamer: streamerUsername='{}', provider='{}'", streamerUsername, providerName);

        TrackingRequestDto request = TrackingRequestDto.builder()
                .streamerUsername(streamerUsername)
                .providerName(providerName)
                .build();

        try {
            TrackStreamerResponse response = restClient.post()
                    .uri(trackerServiceUrl)
                    .body(request)
                    .retrieve()
                    .toEntity(TrackStreamerResponse.class)
                    .getBody();

            validateResponse(response);

            log.debug("Streamer tracked: streamerUsername='{}', provider='{}'", streamerUsername, providerName);
            return response;
        } catch (RestClientException e) {
            throw new TrackerServiceClientException("Failed to track streamer via tracker-service", e);
        }
    }

    private void validateResponse(TrackStreamerResponse response) {
        if (response == null) {
            throw new TrackerServiceClientException("Tracker-service returned invalid response");
        }

        if (response.getStreamerId() == null || response.getStreamerId().toString().isEmpty()) {
            throw new TrackerServiceClientException("Failed to track streamer: tracker-service returned empty streamerId");
        }

        if (response.getProviderName() == null || response.getProviderName().isEmpty()) {
            throw new TrackerServiceClientException("Failed to track streamer: tracker-service returned empty providerName");
        }

        if (response.getStreamerUsername() == null || response.getStreamerUsername().isEmpty()) {
            throw new TrackerServiceClientException("Failed to track streamer: tracker-service returned empty streamerUsername");
        }

        if (response.getStreamerProfileImageUrl() == null || response.getStreamerProfileImageUrl().isEmpty()) {
            throw new TrackerServiceClientException("Failed to track streamer: tracker-service returned empty streamerProfileImageUrl");
        }
    }
}
