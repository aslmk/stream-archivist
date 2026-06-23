package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@Transactional
public class SubscriptionOrchestratorImpl implements SubscriptionOrchestrator {

    private final TrackerServiceClient trackerClient;
    private final UserSubscriptionService userSubscriptionService;
    private final StreamerSubscriptionAggregateService streamerSubscriptionAggregateService;

    public SubscriptionOrchestratorImpl(TrackerServiceClient trackerClient,
                                        UserSubscriptionService userSubscriptionService,
                                        StreamerSubscriptionAggregateService streamerSubscriptionAggregateService) {
        this.trackerClient = trackerClient;
        this.userSubscriptionService = userSubscriptionService;
        this.streamerSubscriptionAggregateService = streamerSubscriptionAggregateService;
    }

    @Override
    public void subscribe(UserRef userRef, StreamerRef streamerRef) {
        UUID subscriberId = UUID.fromString(userRef.id());
        log.debug("Initiating subscribe",
                kv("userId", subscriberId),
                kv("streamerUsername", streamerRef.username()),
                kv("providerName", streamerRef.providerName()));

        TrackStreamerResponse trackedStreamer = trackerClient
                .trackStreamer(streamerRef.username(), streamerRef.providerName());

        if (subscriberId.equals(trackedStreamer.getStreamerId())) {
            throw new IllegalArgumentException(String.format(
                    "Subscriber can't subscribe to himself: userId='%s', streamerId='%s'",
                    subscriberId, trackedStreamer.getStreamerId()));
        }

        CreateUserSubscription userSubscription = buildUserSubscription(trackedStreamer, subscriberId);
        boolean userSubscriptionResult = userSubscriptionService.saveUserSubscription(userSubscription);
        if (!userSubscriptionResult) {
            log.debug("Subscription already exists",
                    kv("userId", userSubscription.getUserId()),
                    kv("streamerId", userSubscription.getStreamerId()));
            return;
        }
        streamerSubscriptionAggregateService.incrementOrCreate(trackedStreamer.getStreamerId());

        log.info("User subscription created",
                kv("userId", subscriberId),
                kv("streamerId", trackedStreamer.getStreamerId()),
                kv("streamerUsername", trackedStreamer.getStreamerUsername()),
                kv("providerName", trackedStreamer.getProviderName()));
    }

    @Override
    public void unsubscribe(String userId, String streamerId) {
        log.debug("Initiating unsubscribe",
                kv("userId", userId),
                kv("streamerId", streamerId));

        userSubscriptionService.deleteUserSubscription(userId, streamerId);

        UUID uuidStreamerId = UUID.fromString(streamerId);
        streamerSubscriptionAggregateService.decrementSubscriptionsCount(uuidStreamerId);
        int subscriptionsCount = streamerSubscriptionAggregateService.getSubscriptionsCount(uuidStreamerId);
        if (subscriptionsCount == 0) {
            trackerClient.unsubscribe(streamerId);
            log.info("Streamer has 0 subscriptions after the last user unsubscribed. " +
                            "System removed webhook for the streamer",
                    kv("userId", userId),
                    kv("streamerId", streamerId));
            return;
        }

        log.debug("User unsubscribed",
                kv("userId", userId),
                kv("streamerId", streamerId),
                kv("remainingSubscriptions", subscriptionsCount));
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
