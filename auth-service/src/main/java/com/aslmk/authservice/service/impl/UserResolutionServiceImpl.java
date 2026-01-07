package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.entity.AccountEntity;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.entity.UserEntity;
import com.aslmk.authservice.exception.InvalidProviderException;
import com.aslmk.authservice.exception.UserNotFoundException;
import com.aslmk.authservice.service.AccountService;
import com.aslmk.authservice.service.UserResolutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserResolutionServiceImpl implements UserResolutionService {

    private final AccountService accountService;

    public UserResolutionServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UUID resolveUserId(String providerUserId, String providerName) {
        log.debug("Resolving user: providerUserId='{}', providerName='{}'", providerUserId, providerName);

        ProviderName provider;
        try {
             provider = ProviderName.valueOf(providerName);
        } catch (IllegalArgumentException e) {
            throw new InvalidProviderException("Invalid provider name: " + providerName);
        }


        Optional<AccountEntity> dbAccount = accountService
                .findByProviderUserIdAndProviderName(providerUserId, provider);

        if (dbAccount.isEmpty()) {
            log.debug("Account not found: providerUserId='{}', providerName='{}'",
                    providerUserId, providerName);
            throw new UserNotFoundException(
                    String.format("Account not found: providerUserId='%s', providerName='%s'",
                            providerUserId, providerName)
            );
        }

        AccountEntity account = dbAccount.get();
        UserEntity user = account.getUser();
        log.debug("Resolved user: id='{}'", user.getId());
        return user.getId();
    }
}
