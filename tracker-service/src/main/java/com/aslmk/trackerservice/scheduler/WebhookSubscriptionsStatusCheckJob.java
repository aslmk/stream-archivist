package com.aslmk.trackerservice.scheduler;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
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

        log.debug("Checking webhook subscription statuses: subscriptions_count='{}'",
                subscriptions.size());

        for (WebhookSubscriptionEntity subscription : subscriptions) {
            try {
                WebhookSubscriptionId id = subscription.getId();
                int currentRetry = subscription.getRetryCount();
                if (currentRetry >= MAX_RETRY_COUNT) {
                    service.updateStatus(subscription.getId(), WebhookSubscriptionStatus.FAILED);
                    log.warn("Max attempts reached; status changed to FAILED: streamerId='{}', subscriptionType='{}', retry={}",
                            id.getStreamerInternalId(), id.getSubscriptionType(), currentRetry);
                    continue;
                }

                TwitchWebhookSubscriptionResponse response = apiClient
                        .getSubscriptionInfo(subscription.getSubscriptionId());

                WebhookSubscriptionStatus status = WebhookSubscriptionStatus.fromValue(response.getStatus());

                if (status == WebhookSubscriptionStatus.PENDING) continue;

                if (status == WebhookSubscriptionStatus.ENABLED) {
                    service.updateStatus(subscription.getId(), WebhookSubscriptionStatus.ENABLED);
                    log.debug("Subscription enabled: type='{}' streamerId='{}'",
                            id.getSubscriptionType(),
                            id.getStreamerInternalId());
                } else {
                    retrySubscription(subscription);
                    log.warn("Retry scheduled for subscription: streamerId='{}', subscriptionType='{}', status='{}'",
                            id.getStreamerInternalId(), id.getSubscriptionType(), status);
                }
            } catch (Exception e) {
                WebhookSubscriptionId id = subscription.getId();
                service.incrementRetryCount(subscription.getId());
                int newRetryCount = subscription.getRetryCount() + 1;

                log.warn("Failed to check subscription (retry={}): type='{}', streamerId='{}'",
                        newRetryCount, id.getSubscriptionType(), id.getStreamerInternalId(), e);

                if (newRetryCount >= MAX_RETRY_COUNT) {
                    service.updateStatus(subscription.getId(), WebhookSubscriptionStatus.FAILED);
                    log.warn("Max attempts reached; status changed to FAILED: streamerId='{}', subscriptionType='{}', retry={}",
                            id.getStreamerInternalId(), id.getSubscriptionType(), newRetryCount);
                }
            }
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void deleteFailedSubscriptions() {
        List<WebhookSubscriptionEntity> subscriptions = service
                .getAllSubscriptionsByStatus(WebhookSubscriptionStatus.FAILED);

        for (WebhookSubscriptionEntity subscription : subscriptions) {
            try {
                apiClient.unsubscribeFromStreamer(subscription.getSubscriptionId(),
                        subscription.getId().getSubscriptionType());
                service.deleteSubscription(subscription.getId());
            } catch (Exception e) {
                WebhookSubscriptionId id = subscription.getId();
                log.warn("Failed to delete subscription from Twitch: streamerId='{}', subscriptionType='{}'",
                        id.getStreamerInternalId(), id.getSubscriptionType(), e);
            }
        }

        log.debug("Deleted {} failed webhook subscriptions",
                subscriptions.size());
    }

    private void retrySubscription(WebhookSubscriptionEntity subscription) {
        apiClient.unsubscribeFromStreamer(subscription.getSubscriptionId(),
                subscription.getId().getSubscriptionType());
        service.resetSubscription(subscription.getId());
    }
}
