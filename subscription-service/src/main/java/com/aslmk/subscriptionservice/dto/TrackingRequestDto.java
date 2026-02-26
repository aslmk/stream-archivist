package com.aslmk.subscriptionservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackingRequestDto {
    private String streamerUsername;
    private String providerName;
}
