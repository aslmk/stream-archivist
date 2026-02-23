package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.common.dto.TrackStreamerResponse;
import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.dto.CreateUserSubscription;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.service.SubscriptionOrchestrator;
import com.aslmk.subscriptionservice.service.SubscriptionService;
import com.aslmk.subscriptionservice.service.UserSubscriptionService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SubscriptionOrchestratorImpl implements SubscriptionOrchestrator {

    private final SubscriptionService subscriptionService;
    private final TrackerServiceClient trackerClient;
    private final UserSubscriptionService userSubscriptionService;

    public SubscriptionOrchestratorImpl(SubscriptionService subscriptionService,
                                        TrackerServiceClient trackerClient, UserSubscriptionService userSubscriptionService) {
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
