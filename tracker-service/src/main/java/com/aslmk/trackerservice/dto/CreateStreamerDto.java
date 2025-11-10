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
    private String providerName;
}
