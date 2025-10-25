package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.dto.CreateAccountDto;
import com.aslmk.authservice.dto.CreateProviderDto;
import com.aslmk.authservice.dto.CreateTokenDto;
import com.aslmk.authservice.dto.OAuthUserInfo;
import com.aslmk.authservice.entity.*;
import com.aslmk.authservice.service.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class OAuthAuthorizationService {

    private final ProviderService providerService;
    private final TokenService tokenService;
    private final UserService userService;
    private final AccountService accountService;
    private final TokenUpdateService tokenUpdateService;

    public OAuthAuthorizationService(ProviderService providerService,
                                     TokenService tokenService,
                                     UserService userService, AccountService accountService, TokenUpdateService tokenUpdateService) {
        this.providerService = providerService;
        this.tokenService = tokenService;
        this.userService = userService;
        this.accountService = accountService;
        this.tokenUpdateService = tokenUpdateService;
    }

    public void authorize(OAuthUserInfo oAuthUserInfo) {
        Optional<AccountEntity> account = accountService.findByProviderUserIdAndProviderName(
                oAuthUserInfo.getProviderUserId(),
                oAuthUserInfo.getProvider()
        );

        if (account.isPresent()) {
            TokenEntity existingToken = account.get().getProvider().getToken();
            tokenUpdateService.updateIfExpired(existingToken);
            return;
        }

        UserEntity createdUser = userService.create();

        ProviderEntity createdProvider = createProvider(oAuthUserInfo.getProviderUserId(),
                oAuthUserInfo.getProvider(),
                createdUser);

        createToken(oAuthUserInfo.getAccessToken(),
                oAuthUserInfo.getRefreshToken(),
                oAuthUserInfo.getExpiresAt(),
                createdProvider);

        createAccount(oAuthUserInfo.getProviderUserId(),
                oAuthUserInfo.getProvider(),
                createdUser,
                createdProvider);
    }

    private void createAccount(String providerUserId,
                               ProviderName providerName,
                               UserEntity user,
                               ProviderEntity provider) {

        CreateAccountDto createAccount = CreateAccountDto.builder()
                .providerUserId(providerUserId)
                .providerName(providerName)
                .user(user)
                .provider(provider)
                .build();

        accountService.create(createAccount);
    }

    private void createToken(String accessToken,
                             String refreshToken,
                             LocalDateTime expiresAt,
                             ProviderEntity provider) {
        CreateTokenDto createToken = CreateTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .provider(provider)
                .build();

        tokenService.create(createToken);
    }

    private ProviderEntity createProvider(String providerUserId,
                                          ProviderName providerName,
                                          UserEntity user) {
        CreateProviderDto createProvider = CreateProviderDto.builder()
                .providerName(providerName)
                .providerUserId(providerUserId)
                .user(user)
                .build();

        return providerService.create(createProvider);
    }

}
