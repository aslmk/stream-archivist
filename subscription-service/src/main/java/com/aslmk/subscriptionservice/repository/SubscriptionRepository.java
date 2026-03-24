package com.aslmk.subscriptionservice.repository;

import com.aslmk.subscriptionservice.domain.SubscriptionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends CrudRepository<SubscriptionEntity, UUID> {
    List<SubscriptionEntity> getAllByUserId(UUID userId);
    void deleteByUserIdAndStreamerId(UUID userId, UUID streamerId);
}
