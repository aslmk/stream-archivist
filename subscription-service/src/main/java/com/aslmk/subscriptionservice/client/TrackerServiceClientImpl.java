package com.aslmk.subscriptionservice.client;

import com.aslmk.common.dto.TrackStreamerResponse;
import com.aslmk.common.dto.TrackingRequestDto;
import com.aslmk.subscriptionservice.exception.TrackerServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
    public UUID trackStreamer(String streamerUsername, String providerName) {
        log.debug("Tracking streamer: streamerUsername='{}', provider='{}'", streamerUsername, providerName);

        TrackingRequestDto request = TrackingRequestDto.builder()
                .streamerUsername(streamerUsername)
                .providerName(providerName)
                .streamQuality("best")
                .build();

        try {
            TrackStreamerResponse response = restClient.post()
                    .uri(trackerServiceUrl)
                    .body(request)
                    .retrieve()
                    .toEntity(TrackStreamerResponse.class)
                    .getBody();

            if (response == null || response.getEntityId() == null) {
                log.error("Failed to track streamer (streamerUsername='{}', provider='{}'): tracker-service returned invalid response", streamerUsername, providerName);
                throw new TrackerServiceClientException("Tracker-service returned invalid response");
            }

            log.debug("Streamer tracked: streamerUsername='{}', provider='{}'", streamerUsername, providerName);
            return response.getEntityId();
        } catch (RestClientException e) {
            throw new TrackerServiceClientException("Failed to track streamer via tracker-service", e);
        }
    }
}
