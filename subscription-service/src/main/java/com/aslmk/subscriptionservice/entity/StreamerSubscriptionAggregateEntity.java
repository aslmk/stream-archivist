package com.aslmk.subscriptionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "streamer_subscriptions_aggregate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamerSubscriptionAggregateEntity {
    @Id
    @Column(nullable = false)
    private UUID streamerId;

    @Column(nullable = false)
    private Integer subscriptionsCount;
}
