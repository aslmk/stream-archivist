package com.aslmk.trackerservice.streamingPlatform.twitch.client;

import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchStreamerInfo;

public interface TwitchApiClient {
    TwitchStreamerInfo getStreamerInfo(String streamerUsername);
    void subscribeToStreamer(String streamerId);
}
