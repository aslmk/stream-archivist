package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookSubscriptionRepository extends
        CrudRepository<WebhookSubscriptionEntity, WebhookSubscriptionId> {

    @Modifying
    @Query(value = """
          UPDATE webhook_subscriptions
          SET subscription_status = :status, updated_at = now()
          WHERE streamer_internal_id = :#{#id.streamerInternalId}
            AND subscription_type = :#{#id.subscriptionType};
          """, nativeQuery = true)
    void updateStatus(@Param("id") WebhookSubscriptionId id, String status);

    @Modifying
    @Query(value = """
           UPDATE webhook_subscriptions
           SET subscription_id = :subscription_id, updated_at = now()
           WHERE streamer_internal_id = :#{#id.streamerInternalId}
             AND subscription_type = :#{#id.subscriptionType};
           """, nativeQuery = true)
    void saveProviderSubscriptionId(@Param("id") WebhookSubscriptionId id,
                                    @Param("subscription_id") UUID providerSubscriptionId);

    @Query(value = "SELECT * FROM webhook_subscriptions WHERE subscription_id IS NULL LIMIT 50;",
            nativeQuery = true)
    List<WebhookSubscriptionEntity> getAllUncreatedSubscriptions();

    @Query(value = """
          SELECT * FROM webhook_subscriptions
          WHERE subscription_status = :status AND subscription_id IS NOT NULL
          LIMIT 50;
          """, nativeQuery = true)
    List<WebhookSubscriptionEntity> getAllSubscriptionsByStatus(String status);

    @Modifying
    @Query(value = """
          UPDATE webhook_subscriptions
          SET subscription_id = NULL,
              retry_count = retry_count + 1, 
              updated_at = now()
          WHERE streamer_internal_id = :#{#id.streamerInternalId}
            AND subscription_type = :#{#id.subscriptionType};
          """, nativeQuery = true)
    void incrementRetryCountAndResetSubscription(@Param("id") WebhookSubscriptionId id);

    @Modifying
    @Query(value = """
          UPDATE webhook_subscriptions
          SET retry_count = retry_count + 1, updated_at = now()
          WHERE streamer_internal_id = :#{#id.streamerInternalId}
            AND subscription_type = :#{#id.subscriptionType};
          """, nativeQuery = true)
    void incrementRetryCount(@Param("id") WebhookSubscriptionId id);
}

