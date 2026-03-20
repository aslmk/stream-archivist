package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.subscriptionservice.dto.TrackStreamerResponse;
import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.dto.CreateUserSubscription;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.service.SubscriptionOrchestrator;
import com.aslmk.subscriptionservice.service.SubscriptionService;
import com.aslmk.subscriptionservice.service.UserSubscriptionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class SubscriptionOrchestratorImpl implements SubscriptionOrchestrator {

    private final SubscriptionService subscriptionService;
    private final TrackerServiceClient trackerClient;
    private final UserSubscriptionService userSubscriptionService;

    public SubscriptionOrchestratorImpl(SubscriptionService subscriptionService,
                                        TrackerServiceClient trackerClient,
                                        UserSubscriptionService userSubscriptionService) {
        this.subscriptionService = subscriptionService;
        this.trackerClient = trackerClient;
        this.userSubscriptionService = userSubscriptionService;
    }

    @Override
    public void subscribe(UserRef userRef, StreamerRef streamerRef) {
        UUID subscriberId = UUID.fromString(userRef.id());
        TrackStreamerResponse trackedStreamer = trackerClient
                .trackStreamer(streamerRef.username(), streamerRef.providerName());

        CreateSubscriptionDto subscription = CreateSubscriptionDto.builder()
                .subscriberId(subscriberId)
                .streamerId(trackedStreamer.getStreamerId())
                .build();

        subscriptionService.subscribe(subscription);

        CreateUserSubscription userSubscription = buildUserSubscription(trackedStreamer, subscriberId);

        userSubscriptionService.saveUserSubscription(userSubscription);
    }

    @Override
    public void unsubscribe(String userId, String streamerId) {
        subscriptionService.unsubscribe(userId, streamerId);
        userSubscriptionService.deleteUserSubscription(userId, streamerId);
        trackerClient.unsubscribe(streamerId);
    }

    private CreateUserSubscription buildUserSubscription(TrackStreamerResponse trackedStreamer, UUID userId) {
        return CreateUserSubscription.builder()
                .userId(userId)
                .streamerId(trackedStreamer.getStreamerId())
                .providerName(trackedStreamer.getProviderName())
                .streamerProfileImageUrl(trackedStreamer.getStreamerProfileImageUrl())
                .streamerUsername(trackedStreamer.getStreamerUsername())
                .build();
    }
}
