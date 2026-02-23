package com.aslmk.subscriptionservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserSubscription {
    private UUID userId;
    private UUID streamerId;
    private String streamerUsername;
    private String streamerProfileImageUrl;
    private String providerName;
}
