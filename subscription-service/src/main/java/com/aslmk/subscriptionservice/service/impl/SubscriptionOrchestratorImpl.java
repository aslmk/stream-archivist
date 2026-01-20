package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.subscriptionservice.client.AuthServiceClient;
import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.service.SubscriptionOrchestrator;
import com.aslmk.subscriptionservice.service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SubscriptionOrchestratorImpl implements SubscriptionOrchestrator {

    private final SubscriptionService subscriptionService;
    private final AuthServiceClient authClient;
    private final TrackerServiceClient trackerClient;

    public SubscriptionOrchestratorImpl(SubscriptionService subscriptionService,
                                        AuthServiceClient authClient,
                                        TrackerServiceClient trackerClient) {
        this.subscriptionService = subscriptionService;
        this.authClient = authClient;
        this.trackerClient = trackerClient;
    }

    @Override
    public void subscribe(UserRef userRef, StreamerRef streamerRef) {
        UUID subscriberId = authClient.resolveUserId(userRef.id(), userRef.providerName());
        UUID streamerId = trackerClient.trackStreamer(streamerRef.username(), streamerRef.providerName());

        CreateSubscriptionDto subscription = CreateSubscriptionDto.builder()
                .subscriberId(subscriberId)
                .streamerId(streamerId)
                .build();

        subscriptionService.subscribe(subscription);
    }
}
