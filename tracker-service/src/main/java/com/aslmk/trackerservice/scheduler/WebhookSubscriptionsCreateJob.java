package com.aslmk.trackerservice.scheduler;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import com.aslmk.trackerservice.dto.CreateTrackingSubscriptionDto;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.service.subscription.StreamTrackingSubscriptionService;
import com.aslmk.trackerservice.service.subscription.WebhookSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Slf4j
public class WebhookSubscriptionsCreateJob {

    private final StreamTrackingSubscriptionService trackingSubscriptionService;
    private final WebhookSubscriptionService service;
    private final TwitchApiClient apiClient;

    public WebhookSubscriptionsCreateJob(StreamTrackingSubscriptionService trackingSubscriptionService,
                                         WebhookSubscriptionService service,
                                         TwitchApiClient apiClient) {
        this.trackingSubscriptionService = trackingSubscriptionService;
        this.service = service;
        this.apiClient = apiClient;
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void createSubscriptions() {
        List<WebhookSubscriptionEntity> uncreatedSubscriptions = service.getAllUncreatedSubscriptions();
        int successfulSubscriptions = 0;

        for (WebhookSubscriptionEntity subscriptionEntity : uncreatedSubscriptions) {
            try {
                createWebhookSubscription(subscriptionEntity);
                successfulSubscriptions++;
            } catch (Exception e) {
                WebhookSubscriptionId id = subscriptionEntity.getId();
                log.warn("Failed to create webhook subscription",
                        kv("subscriptionType", id.getSubscriptionType()),
                        kv("streamerId", id.getStreamerInternalId()),
                        e);
            }
        }

        log.debug("Webhook subscriptions created",
                kv("successfulSubscriptions", successfulSubscriptions),
                kv("allUncreatedSubscriptions", uncreatedSubscriptions.size()));
    }


    private void createWebhookSubscription(WebhookSubscriptionEntity entity) {
        service.updateStatus(entity.getId(), WebhookSubscriptionStatus.PENDING);

        TwitchWebhookSubscriptionResponse response = apiClient
                .subscribeToStreamer(entity.getStreamerProviderId(), entity.getId().getSubscriptionType());

        CreateTrackingSubscriptionDto dto = CreateTrackingSubscriptionDto.builder()
                .subscriptionId(response.getId())
                .subscriptionType(response.getType())
                .providerName(entity.getProviderName())
                .streamerInternalId(entity.getId().getStreamerInternalId())
                .build();

        trackingSubscriptionService.saveSubscription(dto);

        service.saveProviderSubscriptionId(entity.getId(), response.getId());
    }
}
