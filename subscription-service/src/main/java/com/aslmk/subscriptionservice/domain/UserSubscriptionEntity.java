package com.aslmk.subscriptionservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @Column(nullable = false, name = "streamer_username")
    private String streamerUsername;

    @Column(nullable = false, name = "streamer_profile_image_url")
    private String streamerProfileImageUrl;

    @Column(nullable = false, name = "provider_name")
    private String providerName;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


