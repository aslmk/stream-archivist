package com.aslmk.subscriptionservice.client;

import com.aslmk.common.dto.TrackStreamerResponse;

public interface TrackerServiceClient {
    TrackStreamerResponse trackStreamer(String streamerUsername, String providerName);
}
