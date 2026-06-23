package com.aslmk.subscriptionservice;

import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.*;
import com.aslmk.subscriptionservice.exception.TrackerServiceClientException;
import com.aslmk.subscriptionservice.service.StreamerSubscriptionAggregateService;
import com.aslmk.subscriptionservice.service.SubscriptionOrchestratorImpl;
import com.aslmk.subscriptionservice.service.UserSubscriptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class SubscriptionOrchestratorUnitTests {

    @Mock
    private UserSubscriptionService userSubscriptionService;

    @Mock
    private TrackerServiceClient trackerClient;

    @Mock
    private StreamerSubscriptionAggregateService streamerSubscriptionAggregateService;

    @InjectMocks
    private SubscriptionOrchestratorImpl orchestrator;

    @Test
    void subscribe_shouldCallAllDownstreamServices_withCorrectData() {
        Mockito.when(userSubscriptionService.saveUserSubscription(Mockito.any())).thenReturn(true);

        UUID userId = UUID.randomUUID();
        UUID streamerId = UUID.randomUUID();

        UserRef userRef = new UserRef(userId.toString());
        StreamerRef streamerRef = new StreamerRef("456", "twitch");

        TrackStreamerResponse trackedStreamer = TrackStreamerResponse.builder()
                .streamerId(streamerId)
                .streamerUsername("456")
                .providerName("twitch")
                .streamerProfileImageUrl("profile_image_url")
                .build();

        Mockito.when(trackerClient.trackStreamer(streamerRef.username(), streamerRef.providerName()))
                .thenReturn(trackedStreamer);

        orchestrator.subscribe(userRef, streamerRef);

        ArgumentCaptor<CreateUserSubscription> userSubCaptor =
                ArgumentCaptor.forClass(CreateUserSubscription.class);

        Mockito.verify(userSubscriptionService).saveUserSubscription(userSubCaptor.capture());

        Assertions.assertAll(
                () -> Assertions.assertEquals(userId, userSubCaptor.getValue().getUserId()),
                () -> Assertions.assertEquals(streamerId, userSubCaptor.getValue().getStreamerId()),
                () -> Assertions.assertEquals("twitch", userSubCaptor.getValue().getProviderName()),
                () -> Assertions.assertEquals("profile_image_url", userSubCaptor.getValue().getStreamerProfileImageUrl())
        );

        Mockito.verify(streamerSubscriptionAggregateService).incrementOrCreate(streamerId);
    }

    @Test
    void subscribe_shouldNotCallDownstreamServices_whenTrackerFails() {
        UserRef userRef = new UserRef(UUID.randomUUID().toString());
        StreamerRef streamerRef = new StreamerRef("456", "twitch");

        Mockito.when(trackerClient.trackStreamer(streamerRef.username(), streamerRef.providerName()))
                .thenThrow(new TrackerServiceClientException("Streamer not found"));

        Assertions.assertThrows(TrackerServiceClientException.class,
                () -> orchestrator.subscribe(userRef, streamerRef));

        Mockito.verifyNoInteractions(userSubscriptionService, streamerSubscriptionAggregateService);
    }

    @Test
    void subscribe_shouldReturnEarly_whenAlreadySubscribed() {
        UUID userId = UUID.randomUUID();
        UUID streamerId = UUID.randomUUID();

        UserRef userRef = new UserRef(userId.toString());
        StreamerRef streamerRef = new StreamerRef("456", "twitch");

        TrackStreamerResponse trackedStreamer = TrackStreamerResponse.builder()
                .streamerId(streamerId)
                .streamerUsername("456")
                .providerName("twitch")
                .streamerProfileImageUrl("profile_image_url")
                .build();

        Mockito.when(trackerClient.trackStreamer(streamerRef.username(), streamerRef.providerName()))
                .thenReturn(trackedStreamer);

        orchestrator.subscribe(userRef, streamerRef);

        Mockito.verify(userSubscriptionService).saveUserSubscription(Mockito.any());
        Mockito.verifyNoInteractions(streamerSubscriptionAggregateService);
    }

    @Test
    void unsubscribe_shouldDeleteSubscriptionAndDecrementAggregate_whenSubscribersRemain() {
        String userId = UUID.randomUUID().toString();
        String streamerId = UUID.randomUUID().toString();

        Mockito.when(streamerSubscriptionAggregateService.getSubscriptionsCount(UUID.fromString(streamerId)))
                .thenReturn(1);

        orchestrator.unsubscribe(userId, streamerId);

        Mockito.verify(userSubscriptionService).deleteUserSubscription(userId, streamerId);
        Mockito.verify(streamerSubscriptionAggregateService).decrementSubscriptionsCount(UUID.fromString(streamerId));
        Mockito.verify(streamerSubscriptionAggregateService).getSubscriptionsCount(UUID.fromString(streamerId));
        Mockito.verify(trackerClient, Mockito.never()).unsubscribe(streamerId);
    }

    @Test
    void unsubscribe_shouldCallTrackerUnsubscribe_whenLastSubscriberLeaves() {
        String userId = UUID.randomUUID().toString();
        String streamerId = UUID.randomUUID().toString();

        Mockito.when(streamerSubscriptionAggregateService.getSubscriptionsCount(UUID.fromString(streamerId)))
                .thenReturn(0);

        orchestrator.unsubscribe(userId, streamerId);

        Mockito.verify(streamerSubscriptionAggregateService).decrementSubscriptionsCount(UUID.fromString(streamerId));
        Mockito.verify(streamerSubscriptionAggregateService).getSubscriptionsCount(UUID.fromString(streamerId));
        Mockito.verify(trackerClient).unsubscribe(streamerId);
    }
}