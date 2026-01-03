package com.aslmk.subscriptionservice.repository;

import com.aslmk.subscriptionservice.entity.SubscriptionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends CrudRepository<SubscriptionEntity, UUID> {
    Optional<SubscriptionEntity> findByUserId(UUID userId);
    Optional<SubscriptionEntity> findByStreamerId(UUID streamerId);
}
