package com.aslmk.trackerservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteBroadcasterRequestDto {
    private String username;
    private String platform;
}

