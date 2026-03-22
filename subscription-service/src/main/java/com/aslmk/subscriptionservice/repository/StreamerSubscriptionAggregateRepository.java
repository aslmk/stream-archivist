package com.aslmk.subscriptionservice.repository;

import com.aslmk.subscriptionservice.entity.StreamerSubscriptionAggregateEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StreamerSubscriptionAggregateRepository extends
        CrudRepository<StreamerSubscriptionAggregateEntity, UUID> {
    int incrementSubscriptionCountAndGetSubscriptionCountByStreamerId(UUID streamerId);
    int decrementSubscriptionCountAndGetSubscriptionCountByStreamerId(UUID streamerId);
}
