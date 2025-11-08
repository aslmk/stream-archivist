package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.client.AuthServiceClient;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.dto.UserInfoDto;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.service.TrackingService;
import com.aslmk.trackerservice.streamingPlatform.twitch.client.TwitchApiClient;
import org.springframework.stereotype.Service;

@Service
public class TrackingServiceImpl implements TrackingService {

    private final TwitchApiClient twitchClient;
    private final AuthServiceClient authClient;

    public TrackingServiceImpl(TwitchApiClient twitchClient, AuthServiceClient authClient) {
        this.twitchClient = twitchClient;
        this.authClient = authClient;
    }

    @Override
    public void trackStreamer(UserInfoDto userInfo, TrackingRequestDto trackingRequest) {
        validateTrackingRequest(trackingRequest);

        // TODO: Check if the streamer with the specified username already exists in the database
        // TODO: If yes, use provider_user_id from DB
        // TODO: If no, fetch streamer info from Twitch API, save it in the DB, and use its provider_user_id

        String userAccessToken = authClient.fetchUserAccessToken(userInfo);
        String streamerId = twitchClient.getStreamerId(trackingRequest.getStreamerUsername(), userAccessToken);
        twitchClient.subscribeToStreamer(streamerId, userAccessToken);
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
}
