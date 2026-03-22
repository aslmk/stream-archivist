package com.aslmk.subscriptionservice.service;

import java.util.UUID;

public interface StreamerSubscriptionAggregateService {
    void incrementOrCreate(UUID streamerId);
    void decrementSubscriptionsCount(UUID streamerId);
    int getSubscriptionsCount(UUID streamerId);
}
