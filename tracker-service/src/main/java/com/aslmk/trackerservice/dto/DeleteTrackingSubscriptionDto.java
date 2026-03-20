package com.aslmk.trackerservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteTrackingSubscriptionDto {
    private UUID subscriptionId;
}
