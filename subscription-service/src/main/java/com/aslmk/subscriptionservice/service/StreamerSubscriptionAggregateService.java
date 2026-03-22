package com.aslmk.subscriptionservice.service;

import java.util.UUID;

public interface StreamerSubscriptionAggregateService {
    int incrementOrCreate(UUID streamerId);
    int decrementSubscriptionCount(UUID streamerId);
}
