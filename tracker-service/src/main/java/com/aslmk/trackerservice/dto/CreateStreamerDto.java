package com.aslmk.trackerservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateStreamerDto {
    private String username;
    private String streamerId;
    private String profileImageUrl;
    private String providerName;
}
