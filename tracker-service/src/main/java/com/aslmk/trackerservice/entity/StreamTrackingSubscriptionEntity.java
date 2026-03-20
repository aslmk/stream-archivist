package com.aslmk.trackerservice.entity;

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
    private UUID subscriptionId;

    @Column(nullable = false)
    private UUID streamerId;

    @Column(nullable = false)
    private String subscriptionType;

    @Column(nullable = false)
    private String providerName;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
