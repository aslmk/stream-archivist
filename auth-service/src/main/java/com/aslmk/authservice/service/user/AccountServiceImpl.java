package com.aslmk.authservice.service.user;

import com.aslmk.authservice.dto.CreateAccountDto;
import com.aslmk.authservice.domain.user.AccountEntity;
import com.aslmk.authservice.domain.auth.ProviderName;
import com.aslmk.authservice.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<AccountEntity> findByProviderUserIdAndProviderName(String providerUserId, ProviderName provider) {
        return accountRepository.findByProviderUserIdAndProviderName(providerUserId, provider);
    }

    @Override
    public AccountEntity create(CreateAccountDto dto) {
        AccountEntity accountEntity = AccountEntity.builder()
                .providerName(dto.getProviderName())
                .providerUserId(dto.getProviderUserId())
                .user(dto.getUser())
                .provider(dto.getProvider())
                .build();

        return accountRepository.save(accountEntity);
    }
}
