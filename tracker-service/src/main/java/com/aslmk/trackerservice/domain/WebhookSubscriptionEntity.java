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

    @Column(nullable = false)
    private String streamerProviderId;

    @Column(nullable = false)
    private String providerName;

    private UUID subscriptionId;

    @Column(nullable = false)
    private String subscriptionStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer retryCount;
}
