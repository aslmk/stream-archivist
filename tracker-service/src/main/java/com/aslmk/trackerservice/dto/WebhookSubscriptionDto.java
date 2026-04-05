package com.aslmk.trackerservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class WebhookSubscriptionDto {
    private UUID streamerInternalId;
    private String streamerProviderId;
    private String providerName;
    private String subscriptionType;
    private UUID subscriptionId;
    private String subscriptionStatus;
    private Integer retryCount;
}
