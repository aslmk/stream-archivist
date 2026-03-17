package com.aslmk.authservice.service.user;

import com.aslmk.authservice.dto.CreateAccountDto;
import com.aslmk.authservice.domain.user.AccountEntity;
import com.aslmk.authservice.domain.auth.ProviderName;

import java.util.Optional;

public interface AccountService {
    Optional<AccountEntity> findByProviderUserIdAndProviderName(String providerUserId, ProviderName provider);
    AccountEntity create(CreateAccountDto dto);
}
