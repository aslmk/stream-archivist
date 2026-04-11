package com.aslmk.trackerservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stream_tracking_subscriptions")
public class StreamTrackingSubscriptionEntity {
    @Id
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(nullable = false, name = "streamer_id")
    private UUID streamerId;

    @Column(nullable = false, name = "subscription_type")
    private String subscriptionType;

    @Column(nullable = false, name = "provider_name")
    private String providerName;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
