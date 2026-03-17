package com.aslmk.authservice.dto;

import com.aslmk.authservice.domain.auth.ProviderEntity;
import com.aslmk.authservice.domain.auth.ProviderName;
import com.aslmk.authservice.domain.user.UserEntity;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountDto {
    private ProviderName providerName;
    private String providerUserId;
    private UserEntity user;
    private ProviderEntity provider;
}
