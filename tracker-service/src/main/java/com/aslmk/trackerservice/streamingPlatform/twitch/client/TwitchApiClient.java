package com.aslmk.trackerservice.streamingPlatform.twitch.client;

import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchWebhookSubscriptionResponse;

import java.util.UUID;

public interface TwitchApiClient {
    TwitchStreamerInfo getStreamerInfo(String streamerUsername);
    TwitchWebhookSubscriptionResponse subscribeToStreamer(String streamerId, String eventType);
    void unsubscribeFromStreamer(UUID subscriptionId, String eventType);
}
