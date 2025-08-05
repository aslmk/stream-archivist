package com.aslmk.trackerservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteStreamerRequestDto {
    private String username;
    private String platform;
}

