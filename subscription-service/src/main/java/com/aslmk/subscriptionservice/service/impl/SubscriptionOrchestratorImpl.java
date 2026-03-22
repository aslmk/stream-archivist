package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.*;
import com.aslmk.subscriptionservice.service.StreamerSubscriptionAggregateService;
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
    private final StreamerSubscriptionAggregateService streamerSubscriptionAggregateService;

    public SubscriptionOrchestratorImpl(SubscriptionService subscriptionService,
                                        TrackerServiceClient trackerClient,
                                        UserSubscriptionService userSubscriptionService,
                                        StreamerSubscriptionAggregateService streamerSubscriptionAggregateService) {
        this.subscriptionService = subscriptionService;
        this.trackerClient = trackerClient;
        this.userSubscriptionService = userSubscriptionService;
        this.streamerSubscriptionAggregateService = streamerSubscriptionAggregateService;
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

        boolean subscriptionCreated = subscriptionService.subscribe(subscription);

        if (!subscriptionCreated) return;

        CreateUserSubscription userSubscription = buildUserSubscription(trackedStreamer, subscriberId);

        boolean userSubscriptionCreated = userSubscriptionService.saveUserSubscription(userSubscription);

        if (!userSubscriptionCreated) {
            throw new IllegalStateException("UserSubscription not created");
        }

        streamerSubscriptionAggregateService.incrementOrCreate(trackedStreamer.getStreamerId());
    }

    @Override
    public void unsubscribe(String userId, String streamerId) {
        subscriptionService.unsubscribe(userId, streamerId);
        userSubscriptionService.deleteUserSubscription(userId, streamerId);

        UUID uuidStreamerId = UUID.fromString(streamerId);
        streamerSubscriptionAggregateService.decrementSubscriptionsCount(uuidStreamerId);
        int subscriptionsCount = streamerSubscriptionAggregateService.getSubscriptionsCount(uuidStreamerId);
        if (subscriptionsCount == 0) trackerClient.unsubscribe(streamerId);
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
