package com.aslmk.trackerservice.streamingPlatform.twitch.client;

public interface TwitchApiClient {
    String getStreamerId(String streamerUsername);
    void subscribeToStreamer(String streamerId);
}
