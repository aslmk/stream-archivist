package com.aslmk.trackerservice.streamingPlatform.twitch.client;

public interface TwitchApiClient {
    String getStreamerId(String streamerUsername, String userAccessToken);
    void subscribeToStreamer(String streamerId, String userAccessToken);
}
