package com.aslmk.subscriptionservice.repository;

import com.aslmk.subscriptionservice.domain.UserSubscriptionEntity;
import com.aslmk.subscriptionservice.domain.UserSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSubscriptionsRepository extends JpaRepository<UserSubscriptionEntity, UserSubscriptionId> {
    List<UserSubscriptionEntity> findAllById_UserId(UUID userId);
}
