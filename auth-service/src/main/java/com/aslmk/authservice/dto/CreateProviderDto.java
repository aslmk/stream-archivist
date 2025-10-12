package com.aslmk.authservice.dto;


import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.entity.UserEntity;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProviderDto {
    private ProviderName providerName;
    private String providerUserId;
    private UserEntity user;
}
