package com.aslmk.trackerservice.service.subscription;

import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import com.aslmk.trackerservice.dto.WebhookSubscriptionDto;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;

import java.util.List;
import java.util.UUID;

public interface WebhookSubscriptionService {
    void saveSubscription(WebhookSubscriptionDto dto);
    void deleteSubscription(WebhookSubscriptionId subscriptionId);
    List<WebhookSubscriptionEntity> getAllUncreatedSubscriptions();
    void updateStatus(WebhookSubscriptionId id, WebhookSubscriptionStatus status);
    void saveProviderSubscriptionId(WebhookSubscriptionId id, UUID providerSubscriptionId);
    List<WebhookSubscriptionEntity> getAllSubscriptionsByStatus(WebhookSubscriptionStatus status);
    void resetSubscription(WebhookSubscriptionId id);
    void incrementRetryCount(WebhookSubscriptionId subscriptionId);
}
