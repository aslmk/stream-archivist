package com.aslmk.trackerservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "webhook_subscriptions")
public class WebhookSubscriptionEntity {
    @EmbeddedId
    private WebhookSubscriptionId id;

    @Column(nullable = false, name = "streamer_provider_id")
    private String streamerProviderId;

    @Column(nullable = false, name = "provider_name")
    private String providerName;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(nullable = false, name = "subscription_status")
    private String subscriptionStatus;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, name = "retry_count")
    private Integer retryCount;
}
