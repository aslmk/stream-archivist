package com.aslmk.subscriptionservice.repository;

import com.aslmk.subscriptionservice.domain.StreamerSubscriptionAggregateEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamerSubscriptionAggregateRepository extends
        CrudRepository<StreamerSubscriptionAggregateEntity, UUID> {
    @Modifying
    @Query(value = """
            UPDATE streamer_subscriptions_aggregate
            SET subscriptions_count = GREATEST(subscriptions_count - 1, 0)
            WHERE streamer_id = :streamerId;
            """, nativeQuery = true)
    void decrementByStreamerId(UUID streamerId);

    @Modifying
    @Query(value = """
            INSERT INTO streamer_subscriptions_aggregate (streamer_id, subscriptions_count) 
            VALUES (:streamerId, 1) ON CONFLICT (streamer_id) DO 
            UPDATE SET subscriptions_count = EXCLUDED.subscriptions_count + 1; 
            """, nativeQuery = true)
    void incrementOrCreate(UUID streamerId);

    @Query(value = """
            SELECT subscriptions_count FROM streamer_subscriptions_aggregate WHERE streamer_id = :streamerId
            """, nativeQuery = true)
    Optional<Integer> getSubscriptionsCountByStreamerId(UUID streamerId);
}
