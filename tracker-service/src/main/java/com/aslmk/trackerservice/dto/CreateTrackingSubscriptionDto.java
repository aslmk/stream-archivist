package com.aslmk.trackerservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTrackingSubscriptionDto {
    private UUID subscriptionId;
    private String subscriptionType;
    private String providerName;
    private UUID streamerInternalId;
}
