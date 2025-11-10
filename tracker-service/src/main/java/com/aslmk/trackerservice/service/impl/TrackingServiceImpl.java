package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.CreateStreamerDto;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.service.StreamerService;
import com.aslmk.trackerservice.service.TrackingService;
import com.aslmk.trackerservice.streamingPlatform.twitch.client.TwitchApiClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        Optional<StreamerEntity> trackedStreamer = streamerService
                .findByUsername(trackingRequest.getStreamerUsername());

        if (trackedStreamer.isPresent()) {
            return;
        }

        String streamerId = twitchClient.getStreamerId(trackingRequest.getStreamerUsername());

        Optional<StreamerEntity> dbStreamer = streamerService
                .findByProviderUserIdAndProviderName(streamerId, trackingRequest.getProviderName());

        if (dbStreamer.isPresent()) {
            streamerService.updateUsername(dbStreamer.get(), trackingRequest.getStreamerUsername());
            return;
        }

        twitchClient.subscribeToStreamer(streamerId);

        createStreamer(trackingRequest.getStreamerUsername(),
                streamerId,
                trackingRequest.getProviderName()
        );
    }

    private void validateTrackingRequest(TrackingRequestDto trackingRequest) {
        if (trackingRequest == null) {
            throw new TrackingException("Tracking request is null");
        }

        if (trackingRequest.getStreamerUsername() == null || trackingRequest.getStreamerUsername().isBlank()) {
            throw new TrackingException("Streamer username cannot be null or blank");
        }

        if (trackingRequest.getStreamQuality() == null || trackingRequest.getStreamQuality().isBlank()) {
            throw new TrackingException("Stream quality cannot be null or blank");
        }

        if (trackingRequest.getProviderName() == null || trackingRequest.getProviderName().isBlank()) {
            throw new TrackingException("Provider name cannot be null or blank");
        }
    }

    private void createStreamer(String username, String streamerId, String providerName) {
        CreateStreamerDto dto = CreateStreamerDto.builder()
                .username(username)
                .streamerId(streamerId)
                .providerName(providerName)
                .build();

        streamerService.create(dto);
    }
}
