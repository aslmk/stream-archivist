package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.dto.CreateUserSubscription;
import com.aslmk.subscriptionservice.dto.UserSubscriptionsResponse;

public interface UserSubscriptionService {
    UserSubscriptionsResponse getAllUserSubscriptions(String userId);
    boolean saveUserSubscription(CreateUserSubscription dto);
    void deleteUserSubscription(String userId, String streamerId);
}
