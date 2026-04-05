package com.aslmk.trackerservice.dto;

import lombok.Getter;

@Getter
public enum WebhookSubscriptionStatus {
    ENABLED("enabled"),
    PENDING("webhook_callback_verification_pending"),
    FAILED("webhook_callback_verification_failed");

    private final String value;

    WebhookSubscriptionStatus(String value) {
        this.value = value;
    }

    public static WebhookSubscriptionStatus fromValue(String value) {
        for (WebhookSubscriptionStatus status : WebhookSubscriptionStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown webhook status: " + value);
    }
}
