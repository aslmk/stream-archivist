package com.aslmk.authservice.service;

import com.aslmk.authservice.dto.CreateAccountDto;
import com.aslmk.authservice.entity.AccountEntity;
import com.aslmk.authservice.entity.ProviderName;

import java.util.Optional;

public interface AccountService {
    Optional<AccountEntity> findByProviderUserIdAndProviderName(String providerUserId, ProviderName provider);
    AccountEntity create(CreateAccountDto dto);
}
