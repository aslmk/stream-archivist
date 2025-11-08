package com.aslmk.trackerservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDto {
    private String providerUserId;
    private String providerName;
}
