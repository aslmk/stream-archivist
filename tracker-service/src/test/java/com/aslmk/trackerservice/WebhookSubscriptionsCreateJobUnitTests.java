package com.aslmk.trackerservice;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.scheduler.WebhookSubscriptionsCreateJob;
import com.aslmk.trackerservice.service.subscription.StreamTrackingSubscriptionService;
import com.aslmk.trackerservice.service.subscription.WebhookSubscriptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class WebhookSubscriptionsCreateJobUnitTests {

    @Mock
    private StreamTrackingSubscriptionService trackingSubscriptionService;

    @Mock
    private WebhookSubscriptionService webhookSubscriptionService;

    @Mock
    private TwitchApiClient twitchApiClient;

    @InjectMocks
    private WebhookSubscriptionsCreateJob job;

    private static final UUID STREAMER_INTERNAL_ID = UUID.randomUUID();
    private static final UUID PROVIDER_SUBSCRIPTION_ID = UUID.randomUUID();
    private static final String STREAMER_PROVIDER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";
    private static final String EVENT_TYPE_ONLINE = "stream.online";
    private static final String EVENT_TYPE_OFFLINE = "stream.offline";

    private WebhookSubscriptionEntity buildEntity(String eventType) {
        WebhookSubscriptionId id = WebhookSubscriptionId.builder()
                .streamerInternalId(STREAMER_INTERNAL_ID)
                .subscriptionType(eventType)
                .build();
        return WebhookSubscriptionEntity.builder()
                .id(id)
                .streamerProviderId(STREAMER_PROVIDER_ID)
                .providerName(PROVIDER_NAME)
                .build();
    }

    private TwitchWebhookSubscriptionResponse buildTwitchResponse(String eventType) {
        return TwitchWebhookSubscriptionResponse.builder()
                .id(PROVIDER_SUBSCRIPTION_ID)
                .type(eventType)
                .build();
    }

    @Test
    void createSubscriptions_should_doNothing_when_noUncreatedSubscriptionsExist() {
        Mockito.when(webhookSubscriptionService.getAllUncreatedSubscriptions()).thenReturn(List.of());

        job.createSubscriptions();

        Mockito.verify(twitchApiClient, Mockito.never()).subscribeToStreamer(Mockito.any(), Mockito.any());
        Mockito.verify(webhookSubscriptionService, Mockito.never()).updateStatus(Mockito.any(), Mockito.any());
        Mockito.verify(trackingSubscriptionService, Mockito.never()).saveSubscription(Mockito.any());
    }

    @Test
    void createSubscriptions_should_processAllSteps_when_singleSubscriptionExists() {
        WebhookSubscriptionEntity entity = buildEntity(EVENT_TYPE_ONLINE);
        TwitchWebhookSubscriptionResponse twitchResponse = buildTwitchResponse(EVENT_TYPE_ONLINE);

        Mockito.when(webhookSubscriptionService.getAllUncreatedSubscriptions()).thenReturn(List.of(entity));
        Mockito.when(twitchApiClient.subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_ONLINE))
                .thenReturn(twitchResponse);

        job.createSubscriptions();

        Mockito.verify(webhookSubscriptionService).updateStatus(entity.getId(), WebhookSubscriptionStatus.PENDING);
        Mockito.verify(twitchApiClient).subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_ONLINE);
        Mockito.verify(trackingSubscriptionService).saveSubscription(Mockito.argThat(dto ->
                dto.getSubscriptionId().equals(PROVIDER_SUBSCRIPTION_ID) &&
                        dto.getSubscriptionType().equals(EVENT_TYPE_ONLINE) &&
                        dto.getProviderName().equals(PROVIDER_NAME) &&
                        dto.getStreamerInternalId().equals(STREAMER_INTERNAL_ID)
        ));
        Mockito.verify(webhookSubscriptionService)
                .saveProviderSubscriptionId(entity.getId(), PROVIDER_SUBSCRIPTION_ID);
    }

    @Test
    void createSubscriptions_should_processAllSubscriptions_when_multipleExist() {
        WebhookSubscriptionEntity onlineEntity = buildEntity(EVENT_TYPE_ONLINE);
        WebhookSubscriptionEntity offlineEntity = buildEntity(EVENT_TYPE_OFFLINE);

        Mockito.when(webhookSubscriptionService.getAllUncreatedSubscriptions())
                .thenReturn(List.of(onlineEntity, offlineEntity));
        Mockito.when(twitchApiClient.subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_ONLINE))
                .thenReturn(buildTwitchResponse(EVENT_TYPE_ONLINE));
        Mockito.when(twitchApiClient.subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_OFFLINE))
                .thenReturn(buildTwitchResponse(EVENT_TYPE_OFFLINE));

        job.createSubscriptions();

        Mockito.verify(twitchApiClient).subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_ONLINE);
        Mockito.verify(twitchApiClient).subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_OFFLINE);
        Mockito.verify(trackingSubscriptionService, Mockito.times(2))
                .saveSubscription(Mockito.any());
        Mockito.verify(webhookSubscriptionService, Mockito.times(2))
                .saveProviderSubscriptionId(Mockito.any(), Mockito.any());
    }

    @Test
    void createSubscriptions_should_continueProcessing_when_oneSubscriptionFails() {
        WebhookSubscriptionEntity onlineEntity = buildEntity(EVENT_TYPE_ONLINE);
        WebhookSubscriptionEntity offlineEntity = buildEntity(EVENT_TYPE_OFFLINE);

        Mockito.when(webhookSubscriptionService.getAllUncreatedSubscriptions())
                .thenReturn(List.of(onlineEntity, offlineEntity));
        Mockito.when(twitchApiClient.subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_ONLINE))
                .thenThrow(new TwitchApiClientException("Twitch API unavailable"));
        Mockito.when(twitchApiClient.subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_OFFLINE))
                .thenReturn(buildTwitchResponse(EVENT_TYPE_OFFLINE));

        Assertions.assertDoesNotThrow(() -> job.createSubscriptions());

        Mockito.verify(twitchApiClient).subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_ONLINE);
        Mockito.verify(twitchApiClient).subscribeToStreamer(STREAMER_PROVIDER_ID, EVENT_TYPE_OFFLINE);
        Mockito.verify(trackingSubscriptionService, Mockito.times(1))
                .saveSubscription(Mockito.any());
        Mockito.verify(webhookSubscriptionService, Mockito.times(1))
                .saveProviderSubscriptionId(Mockito.any(), Mockito.any());
    }
}