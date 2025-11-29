package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.CreateStreamerDto;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.service.StreamerService;
import com.aslmk.trackerservice.service.TrackingService;
import com.aslmk.trackerservice.streamingPlatform.twitch.client.TwitchApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TrackingServiceImpl implements TrackingService {

    private final TwitchApiClient twitchClient;
    private final StreamerService streamerService;

    public TrackingServiceImpl(TwitchApiClient twitchClient, StreamerService streamerService) {
        this.twitchClient = twitchClient;
        this.streamerService = streamerService;
    }

    @Override
    public void trackStreamer(TrackingRequestDto trackingRequest) {
        validateTrackingRequest(trackingRequest);

        log.info("Start tracking: streamer='{}', provider='{}'",
                trackingRequest.getStreamerUsername(), trackingRequest.getProviderName());

        Optional<StreamerEntity> trackedStreamer = streamerService
                .findByUsername(trackingRequest.getStreamerUsername());

        if (trackedStreamer.isPresent()) {
            log.info("Streamer='{}' already tracked — skipping subscription", trackingRequest.getStreamerUsername());
            return;
        }

        log.debug("Fetching streamer ID from Twitch API for username='{}'", trackingRequest.getStreamerUsername());
        String streamerId = twitchClient.getStreamerId(trackingRequest.getStreamerUsername());
        log.debug("Received Twitch streamerId='{}' for username='{}'", streamerId, trackingRequest.getStreamerUsername());


        Optional<StreamerEntity> dbStreamer = streamerService
                .findByProviderUserIdAndProviderName(streamerId, trackingRequest.getProviderName());

        if (dbStreamer.isPresent()) {
            log.info("Streamer with id='{}' already exists in DB but username differs. Updating username to '{}'",
                    streamerId, trackingRequest.getStreamerUsername());
            streamerService.updateUsername(dbStreamer.get(), trackingRequest.getStreamerUsername());
            return;
        }

        log.info("No existing streamer found — creating webhook subscription for streamerId='{}'", streamerId);
        twitchClient.subscribeToStreamer(streamerId);

        log.info("Creating new streamer entry in DB: username='{}', streamerId='{}', provider='{}'",
                trackingRequest.getStreamerUsername(), streamerId, trackingRequest.getProviderName());
        createStreamer(trackingRequest.getStreamerUsername(),
                streamerId,
                trackingRequest.getProviderName()
        );

        log.info("Tracking setup completed: streamer {}, provider {}",
                trackingRequest.getStreamerUsername(), trackingRequest.getProviderName());
    }

    private void validateTrackingRequest(TrackingRequestDto trackingRequest) {
        if (trackingRequest == null) {
            log.warn("Tracking request validation failed: request is null");
            throw new TrackingException("Tracking request is null");
        }

        if (trackingRequest.getStreamerUsername() == null || trackingRequest.getStreamerUsername().isBlank()) {
            log.warn("Tracking request validation failed: streamer username is null or blank");
            throw new TrackingException("Streamer username cannot be null or blank");
        }

        if (trackingRequest.getStreamQuality() == null || trackingRequest.getStreamQuality().isBlank()) {
            log.warn("Tracking request validation failed: stream quality is null or blank");
            throw new TrackingException("Stream quality cannot be null or blank");
        }

        if (trackingRequest.getProviderName() == null || trackingRequest.getProviderName().isBlank()) {
            log.warn("Tracking request validation failed: provider name is null or blank");
            throw new TrackingException("Provider name cannot be null or blank");
        }
    }

    private void createStreamer(String username, String streamerId, String providerName) {
        CreateStreamerDto dto = CreateStreamerDto.builder()
                .username(username)
                .streamerId(streamerId)
                .providerName(providerName)
                .build();

        log.debug("Saving to DB: streamer={}, streamerId={}, provider={}", username, streamerId, providerName);
        streamerService.create(dto);
    }
}
