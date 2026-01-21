package com.aslmk.common.dto;

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
