package com.aslmk.authservice.dto;

import com.aslmk.authservice.entity.ProviderEntity;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.entity.UserEntity;
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
