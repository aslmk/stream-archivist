package com.aslmk.subscriptionservice.repository;

import com.aslmk.subscriptionservice.entity.UserSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSubscriptionsRepository extends JpaRepository<UserSubscriptionEntity, UUID> {
    List<UserSubscriptionEntity> findAllByUserId(UUID userId);
}
