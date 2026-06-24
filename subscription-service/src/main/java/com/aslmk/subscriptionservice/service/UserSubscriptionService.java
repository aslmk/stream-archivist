package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.dto.CreateUserSubscription;
import com.aslmk.subscriptionservice.dto.UserSubscriptionsResponse;

import java.util.UUID;

public interface UserSubscriptionService {
    UserSubscriptionsResponse getAllUserSubscriptions(UUID userId);
    boolean saveUserSubscription(CreateUserSubscription dto);
    void deleteUserSubscription(UUID userId, UUID streamerId);
}
