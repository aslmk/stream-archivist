package com.aslmk.trackerservice;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.scheduler.WebhookSubscriptionsStatusCheckJob;
import com.aslmk.trackerservice.service.subscription.WebhookSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class WebhookSubscriptionsStatusCheckJobUnitTest {

    @Mock
    private WebhookSubscriptionService service;

    @Mock
    private TwitchApiClient apiClient;

    @InjectMocks
    private WebhookSubscriptionsStatusCheckJob job;

    private static final String SUBSCRIPTION_TYPE = "stream.online";
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";
    private static final int MAX_RETRY_COUNT = 5;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(job, "MAX_RETRY_COUNT", MAX_RETRY_COUNT);
    }

    private WebhookSubscriptionId buildId(UUID streamerId, String type) {
        WebhookSubscriptionId id = new WebhookSubscriptionId();
        id.setStreamerInternalId(streamerId);
        id.setSubscriptionType(type);
        return id;
    }

    private WebhookSubscriptionEntity buildSubscription(int retryCount, UUID subscriptionId) {
        return WebhookSubscriptionEntity.builder()
                .id(buildId(UUID.randomUUID(), SUBSCRIPTION_TYPE))
                .streamerProviderId(PROVIDER_USER_ID)
                .providerName(PROVIDER_NAME)
                .subscriptionId(subscriptionId)
                .subscriptionStatus(WebhookSubscriptionStatus.PENDING.getValue())
                .retryCount(retryCount)
                .build();
    }

    @Test
    void checkSubscriptionsStatus_should_doNothing_when_noSubscriptions() {
        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING))
                .thenReturn(Collections.emptyList());

        job.checkSubscriptionsStatus();

        Mockito.verifyNoInteractions(apiClient);
        Mockito.verify(service).getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING);
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    void checkSubscriptionsStatus_should_markFailed_when_retryCountReachedMax() {
        WebhookSubscriptionEntity sub = buildSubscription(MAX_RETRY_COUNT, UUID.randomUUID());

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING))
                .thenReturn(List.of(sub));

        job.checkSubscriptionsStatus();

        Mockito.verify(service).updateStatus(sub.getId(), WebhookSubscriptionStatus.FAILED);
        Mockito.verifyNoInteractions(apiClient);
    }

    @Test
    void checkSubscriptionsStatus_should_doNothing_when_twitchStatusIsPending() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionEntity sub = buildSubscription(0, subscriptionId);

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING))
                .thenReturn(List.of(sub));
        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenReturn(TwitchWebhookSubscriptionResponse.builder()
                        .status(WebhookSubscriptionStatus.PENDING.getValue())
                        .build());

        job.checkSubscriptionsStatus();

        Mockito.verify(apiClient).getSubscriptionInfo(subscriptionId);
        Mockito.verify(service, Mockito.never()).updateStatus(Mockito.any(), Mockito.any());
        Mockito.verify(service, Mockito.never()).resetSubscription(Mockito.any());
    }

    @Test
    void checkSubscriptionsStatus_should_updateStatusToEnabled_when_twitchStatusIsEnabled() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionEntity sub = buildSubscription(0, subscriptionId);

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING))
                .thenReturn(List.of(sub));
        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenReturn(TwitchWebhookSubscriptionResponse.builder()
                        .status(WebhookSubscriptionStatus.ENABLED.getValue())
                        .build());

        job.checkSubscriptionsStatus();

        Mockito.verify(service).updateStatus(sub.getId(), WebhookSubscriptionStatus.ENABLED);
        Mockito.verify(apiClient, Mockito.never()).unsubscribeFromStreamer(Mockito.any(), Mockito.any());
    }

    @Test
    void checkSubscriptionsStatus_should_unsubscribeAndReset_when_twitchStatusIsFailed() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionEntity sub = buildSubscription(0, subscriptionId);

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING))
                .thenReturn(List.of(sub));
        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenReturn(TwitchWebhookSubscriptionResponse.builder()
                        .status(WebhookSubscriptionStatus.FAILED.getValue())
                        .build());

        job.checkSubscriptionsStatus();

        Mockito.verify(apiClient).unsubscribeFromStreamer(subscriptionId, SUBSCRIPTION_TYPE);
        Mockito.verify(service).resetSubscription(sub.getId());
        Mockito.verify(service, Mockito.never()).updateStatus(Mockito.any(), Mockito.any());
    }

    @Test
    void checkSubscriptionsStatus_should_onlyIncrementRetryCount_when_exceptionThrownAndBelowMax() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionEntity sub = buildSubscription(1, subscriptionId);

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING))
                .thenReturn(List.of(sub));
        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenThrow(new RuntimeException("Twitch API error"));

        job.checkSubscriptionsStatus();

        Mockito.verify(service).incrementRetryCount(sub.getId());
        Mockito.verify(service, Mockito.never()).updateStatus(Mockito.any(), Mockito.any());
    }

    @Test
    void checkSubscriptionsStatus_should_incrementRetryCountAndMarkFailed_when_exceptionThrownAndReachesMax() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionEntity sub = buildSubscription(MAX_RETRY_COUNT - 1, subscriptionId);

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING))
                .thenReturn(List.of(sub));
        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenThrow(new RuntimeException("Twitch API error"));

        job.checkSubscriptionsStatus();

        Mockito.verify(service).incrementRetryCount(sub.getId());
        Mockito.verify(service).updateStatus(sub.getId(), WebhookSubscriptionStatus.FAILED);
    }

    @Test
    void deleteFailedSubscriptions_should_doNothing_when_noFailedSubscriptions() {
        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.FAILED))
                .thenReturn(Collections.emptyList());

        job.deleteFailedSubscriptions();

        Mockito.verifyNoInteractions(apiClient);
        Mockito.verify(service).getAllSubscriptionsByStatus(WebhookSubscriptionStatus.FAILED);
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    void deleteFailedSubscriptions_should_unsubscribeAndDelete_when_failedSubscriptionsExist() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionEntity sub = buildSubscription(MAX_RETRY_COUNT, subscriptionId);

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.FAILED))
                .thenReturn(List.of(sub));

        job.deleteFailedSubscriptions();

        Mockito.verify(apiClient).unsubscribeFromStreamer(subscriptionId, SUBSCRIPTION_TYPE);
        Mockito.verify(service).deleteSubscription(sub.getId());
    }

    @Test
    void deleteFailedSubscriptions_should_skipDeletion_when_apiThrowsException() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionEntity sub = buildSubscription(MAX_RETRY_COUNT, subscriptionId);

        Mockito.when(service.getAllSubscriptionsByStatus(WebhookSubscriptionStatus.FAILED))
                .thenReturn(List.of(sub));
        Mockito.doThrow(new RuntimeException("Twitch API error"))
                .when(apiClient).unsubscribeFromStreamer(subscriptionId, SUBSCRIPTION_TYPE);

        job.deleteFailedSubscriptions();

        Mockito.verify(service, Mockito.never()).deleteSubscription(Mockito.any());
    }
}
