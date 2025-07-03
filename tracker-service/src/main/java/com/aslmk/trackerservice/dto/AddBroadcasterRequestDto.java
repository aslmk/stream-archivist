package com.aslmk.trackerservice.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddBroadcasterRequestDto {
    private String username;
    private String platform;
}
