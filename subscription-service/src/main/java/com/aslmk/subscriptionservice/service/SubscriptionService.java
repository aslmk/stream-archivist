package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.entity.SubscriptionEntity;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionService {
    Optional<SubscriptionEntity> findByUserId(UUID userId);
    Optional<SubscriptionEntity> findByStreamerId(UUID streamerId);
    void subscribe(CreateSubscriptionDto dto);
}
