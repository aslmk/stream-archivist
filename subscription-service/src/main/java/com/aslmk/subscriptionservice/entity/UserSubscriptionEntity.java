package com.aslmk.subscriptionservice.entity;

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
@Table(name = "user_subscriptions")
public class UserSubscriptionEntity {
    @EmbeddedId
    private UserSubscriptionId id;

    @Column(nullable = false)
    private String streamerUsername;

    @Column(nullable = false)
    private String streamerProfileImageUrl;

    @Column(nullable = false)
    private String providerName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


