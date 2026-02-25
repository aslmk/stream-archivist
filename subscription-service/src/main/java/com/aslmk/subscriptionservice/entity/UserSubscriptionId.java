package com.aslmk.subscriptionservice.entity;

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
public class UserSubscriptionId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "streamer_id", nullable = false)
    private UUID streamerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSubscriptionId that = (UserSubscriptionId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(streamerId, that.streamerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, streamerId);
    }
}
