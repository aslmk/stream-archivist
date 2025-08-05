package com.aslmk.trackerservice.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddStreamerRequestDto {
    private String username;
    private String platform;
}
