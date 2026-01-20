package com.aslmk.subscriptionservice.client;

import java.util.UUID;

public interface TrackerServiceClient {
    UUID trackStreamer(String streamerUsername, String providerName);
}
