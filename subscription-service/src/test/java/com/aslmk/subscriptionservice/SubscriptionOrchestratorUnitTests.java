package com.aslmk.subscriptionservice;

import com.aslmk.subscriptionservice.client.AuthServiceClient;
import com.aslmk.subscriptionservice.client.TrackerServiceClient;
import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.exception.AuthServiceClientException;
import com.aslmk.subscriptionservice.exception.TrackerServiceClientException;
import com.aslmk.subscriptionservice.service.SubscriptionService;
import com.aslmk.subscriptionservice.service.impl.SubscriptionOrchestratorImpl;
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
    private SubscriptionService subscriptionService;
    @Mock
    private AuthServiceClient authClient;
    @Mock
    private TrackerServiceClient trackerClient;

    @InjectMocks
    private SubscriptionOrchestratorImpl orchestrator;

    @Test
    void should_resolveUserAndStreamer_andSaveSubscription() {
        UserRef userRef = new UserRef("123", "twitch");
        StreamerRef streamerRef = new StreamerRef("456", "twitch");

        UUID userId = UUID.randomUUID();
        UUID streamerId = UUID.randomUUID();

        ArgumentCaptor<CreateSubscriptionDto> captor = ArgumentCaptor.forClass(CreateSubscriptionDto.class);

        Mockito.when(authClient.resolveUserId(userRef.id(), userRef.providerName()))
                .thenReturn(userId);
        Mockito.when(trackerClient.resolveStreamerId(streamerRef.id(), streamerRef.providerName()))
                .thenReturn(streamerId);

        orchestrator.subscribe(userRef, streamerRef);

        Mockito.verify(authClient).resolveUserId(userRef.id(), userRef.providerName());
        Mockito.verify(trackerClient).resolveStreamerId(streamerRef.id(), streamerRef.providerName());
        Mockito.verify(subscriptionService).subscribe(captor.capture());

        Assertions.assertEquals(userId, captor.getValue().getSubscriberId());
        Assertions.assertEquals(streamerId, captor.getValue().getStreamerId());
    }


    @Test
    void should_stop_whenAuthServiceFails() {
        UserRef userRef = new UserRef("123", "twitch");
        StreamerRef streamerRef = new StreamerRef("456", "twitch");

        Mockito.when(authClient.resolveUserId(userRef.id(), userRef.providerName()))
                .thenThrow(new AuthServiceClientException("Auth failed"));

        Assertions.assertThrows(AuthServiceClientException.class,
                () -> orchestrator.subscribe(userRef, streamerRef));

        Mockito.verify(authClient).resolveUserId(userRef.id(), userRef.providerName());
        Mockito.verifyNoInteractions(trackerClient, subscriptionService);
    }

    @Test
    void subscribe_shouldNotSave_whenTrackerServiceFails() {
        UserRef userRef = new UserRef("123", "twitch");
        StreamerRef streamerRef = new StreamerRef("456", "twitch");

        UUID userId = UUID.randomUUID();

        Mockito.when(authClient.resolveUserId(userRef.id(), userRef.providerName())).thenReturn(userId);
        Mockito.when(trackerClient.resolveStreamerId(streamerRef.id(), streamerRef.providerName()))
                .thenThrow(new TrackerServiceClientException("Streamer not found"));

        Assertions.assertThrows(TrackerServiceClientException.class,
                () -> orchestrator.subscribe(userRef, streamerRef));

        Mockito.verify(authClient).resolveUserId(userRef.id(), userRef.providerName());
        Mockito.verify(trackerClient).resolveStreamerId(streamerRef.id(), streamerRef.providerName());
        Mockito.verifyNoInteractions(subscriptionService);
    }


}
