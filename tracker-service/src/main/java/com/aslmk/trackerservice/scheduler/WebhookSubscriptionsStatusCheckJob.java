package com.aslmk.trackerservice.scheduler;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.service.subscription.WebhookSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebhookSubscriptionsStatusCheckJob {

    @Value("${user.webhook-subscription.retry-max-count}")
    private int MAX_RETRY_COUNT;


    private final WebhookSubscriptionService service;
    private final TwitchApiClient apiClient;

    public WebhookSubscriptionsStatusCheckJob(WebhookSubscriptionService service,
                                              TwitchApiClient apiClient) {
        this.service = service;
        this.apiClient = apiClient;
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void checkSubscriptionsStatus() {
        List<WebhookSubscriptionEntity> subscriptions = service
                .getAllSubscriptionsByStatus(WebhookSubscriptionStatus.PENDING);

        log.info("Checking webhook subscriptions status: subscriptions_count='{}'", subscriptions.size());

        for (WebhookSubscriptionEntity subscription : subscriptions) {
            try {
                if (subscription.getRetryCount() >= MAX_RETRY_COUNT) {
                    log.warn("Webhook subscription retry count={}", subscription.getRetryCount());
                    service.updateStatus(subscription.getId(), WebhookSubscriptionStatus.FAILED);
                    continue;
                }

                TwitchWebhookSubscriptionResponse response = apiClient
                        .getSubscriptionInfo(subscription.getSubscriptionId());

                WebhookSubscriptionStatus status = WebhookSubscriptionStatus.fromValue(response.getStatus());

                if (status == WebhookSubscriptionStatus.PENDING) continue;

                if (status == WebhookSubscriptionStatus.ENABLED) {
                    log.info("Subscription of type '{}' enabled: subscriptionId='{}', streamerId='{}'",
                            subscription.getId().getSubscriptionType(),
                            subscription.getSubscriptionId(),
                            subscription.getId().getStreamerInternalId());

                    service.updateStatus(subscription.getId(), WebhookSubscriptionStatus.ENABLED);
                } else {
                    retrySubscription(subscription);
                }
            } catch (Exception e) {
                log.error("Failed to check subscription status: subscriptionId='{}'",
                        subscription.getSubscriptionId(), e);

                service.incrementRetryCount(subscription.getId());
                int newRetryCount = subscription.getRetryCount() + 1;

                if (newRetryCount >= MAX_RETRY_COUNT) {
                    service.updateStatus(subscription.getId(), WebhookSubscriptionStatus.FAILED);
                }

            }
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void deleteFailedSubscriptions() {
        List<WebhookSubscriptionEntity> subscriptions = service
                .getAllSubscriptionsByStatus(WebhookSubscriptionStatus.FAILED);

        log.info("Deleting failed webhook subscriptions: subscriptions_count='{}'", subscriptions.size());

        for (WebhookSubscriptionEntity subscription : subscriptions) {
            try {
                apiClient.unsubscribeFromStreamer(subscription.getSubscriptionId(),
                        subscription.getId().getSubscriptionType());
                service.deleteSubscription(subscription.getId());
            } catch (Exception e) {
                log.error("Failed to delete subscription from Twitch: subscriptionId='{}'",
                        subscription.getSubscriptionId(), e);
            }
        }
    }

    private void retrySubscription(WebhookSubscriptionEntity subscription) {
        apiClient.unsubscribeFromStreamer(subscription.getSubscriptionId(),
                subscription.getId().getSubscriptionType());
        service.resetSubscription(subscription.getId());
    }
}
