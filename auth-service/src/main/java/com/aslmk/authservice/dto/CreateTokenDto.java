package com.aslmk.authservice.dto;


import com.aslmk.authservice.entity.ProviderEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTokenDto {
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private ProviderEntity provider;
}
