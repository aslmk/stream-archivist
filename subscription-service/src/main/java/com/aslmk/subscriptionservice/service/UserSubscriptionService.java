package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.dto.CreateUserSubscription;
import com.aslmk.subscriptionservice.dto.UserSubscriptionsResponse;
import com.aslmk.subscriptionservice.entity.UserSubscriptionEntity;

public interface UserSubscriptionService {
    UserSubscriptionsResponse getAllUserSubscriptions(String userId);
    UserSubscriptionEntity saveUserSubscription(CreateUserSubscription dto);
}
