package com.aslmk.trackerservice.client.twitch;

import com.aslmk.trackerservice.client.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;

import java.util.UUID;

public interface TwitchApiClient {
    TwitchStreamerInfo getStreamerInfo(String streamerUsername);
    TwitchWebhookSubscriptionResponse subscribeToStreamer(String streamerId, String eventType);
    void unsubscribeFromStreamer(UUID subscriptionId, String eventType);
}
