package com.aslmk.subscriptionservice.client;

import com.aslmk.subscriptionservice.dto.TrackStreamerResponse;

public interface TrackerServiceClient {
    TrackStreamerResponse trackStreamer(String streamerUsername, String providerName);
}
