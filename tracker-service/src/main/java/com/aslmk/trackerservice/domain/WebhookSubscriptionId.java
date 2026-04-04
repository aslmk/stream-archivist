package com.aslmk.trackerservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class WebhookSubscriptionId implements Serializable {
    @Column(name = "streamer_internal_id", nullable = false)
    private UUID streamerInternalId;
    @Column(name = "subscription_type", nullable = false)
    private String subscriptionType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebhookSubscriptionId that = (WebhookSubscriptionId) o;
        return Objects.equals(streamerInternalId, that.streamerInternalId) && Objects.equals(subscriptionType, that.subscriptionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamerInternalId, subscriptionType);
    }
}
