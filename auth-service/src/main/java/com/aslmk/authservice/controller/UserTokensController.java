package com.aslmk.authservice.controller;

import com.aslmk.authservice.entity.AccountEntity;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/internal")
public class UserTokensController {

    private final AccountService accountService;

    public UserTokensController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{provider}/users/{providerUserId}/token")
    public ResponseEntity<String> getUserToken(@PathVariable String provider,
                                       @PathVariable String providerUserId) {
        ProviderName providerName = getProviderName(provider);

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Provider user id cannot be null or blank");
        }

        Optional<AccountEntity> account = accountService
                .findByProviderUserIdAndProviderName(providerUserId, providerName);

        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AccountEntity accountEntity = account.get();
        return ResponseEntity.ok(accountEntity.getProvider().getToken().getAccessToken());
    }

    private ProviderName getProviderName(String provider) {
        try {
            return ProviderName.valueOf(provider);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid provider name: " + provider);
        }
    }
}
