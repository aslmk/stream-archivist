package com.aslmk.subscriptionservice.client;

import com.aslmk.subscriptionservice.dto.TrackStreamerResponse;

import java.util.UUID;

public interface TrackerServiceClient {
    TrackStreamerResponse trackStreamer(String streamerUsername, String providerName);
    void unsubscribe(UUID streamerId);
}
