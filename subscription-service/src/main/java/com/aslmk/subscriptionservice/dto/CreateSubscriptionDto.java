package com.aslmk.subscriptionservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSubscriptionDto {
    private UUID subscriberId;
    private UUID streamerId;
}
