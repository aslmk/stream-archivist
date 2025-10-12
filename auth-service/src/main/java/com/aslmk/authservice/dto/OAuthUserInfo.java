package com.aslmk.authservice.dto;


import com.aslmk.authservice.entity.ProviderName;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserInfo {
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private ProviderName provider;
    private String providerUserId;
}
