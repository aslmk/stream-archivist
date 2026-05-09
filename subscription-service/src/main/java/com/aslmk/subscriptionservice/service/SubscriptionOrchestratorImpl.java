package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
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
        log.debug("Initiating subscribe. userId='{}', streamerUsername='{}', providerName='{}'",
                subscriberId, streamerRef.username(), streamerRef.providerName());

        TrackStreamerResponse trackedStreamer = trackerClient
                .trackStreamer(streamerRef.username(), streamerRef.providerName());

        CreateSubscriptionDto subscription = CreateSubscriptionDto.builder()
                .subscriberId(subscriberId)
                .streamerId(trackedStreamer.getStreamerId())
                .build();

        boolean subscriptionCreated = subscriptionService.subscribe(subscription);

        if (!subscriptionCreated) {
            log.debug("Subscription already exists. userId='{}', streamerId='{}'",
                    subscriberId, trackedStreamer.getStreamerId());
            return;
        }

        CreateUserSubscription userSubscription = buildUserSubscription(trackedStreamer, subscriberId);

        boolean userSubscriptionCreated = userSubscriptionService.saveUserSubscription(userSubscription);

        if (!userSubscriptionCreated) {
            throw new IllegalStateException(String.format(
                    "User subscription not created: userId='%s', streamerId='%s', streamerUsername='%s'",
                    subscriberId, trackedStreamer.getStreamerId(), trackedStreamer.getStreamerUsername()));
        }

        streamerSubscriptionAggregateService.incrementOrCreate(trackedStreamer.getStreamerId());

        log.info("User subscription created: userId='{}', streamerId='{}', streamerUsername='{}', provider='{}'",
                subscriberId,
                trackedStreamer.getStreamerId(),
                trackedStreamer.getStreamerUsername(),
                trackedStreamer.getProviderName());
    }

    @Override
    public void unsubscribe(String userId, String streamerId) {
        log.debug("Initiating unsubscribe. userId='{}', streamerId='{}'", userId, streamerId);

        subscriptionService.unsubscribe(userId, streamerId);
        userSubscriptionService.deleteUserSubscription(userId, streamerId);

        UUID uuidStreamerId = UUID.fromString(streamerId);
        streamerSubscriptionAggregateService.decrementSubscriptionsCount(uuidStreamerId);
        int subscriptionsCount = streamerSubscriptionAggregateService.getSubscriptionsCount(uuidStreamerId);
        if (subscriptionsCount == 0) {
            trackerClient.unsubscribe(streamerId);
            log.info("Streamer has 0 subscriptions after user='{}' unsubscribed. System removed webhook for streamer='{}'",
                    userId, streamerId);
            return;
        }

        log.debug("User='{}' successfully unsubscribed. Streamer='{}' remaining subscriptions='{}'",
                userId, streamerId, subscriptionsCount);
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
