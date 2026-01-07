package com.aslmk.authservice;

import com.aslmk.authservice.entity.AccountEntity;
import com.aslmk.authservice.entity.ProviderName;
import com.aslmk.authservice.entity.UserEntity;
import com.aslmk.authservice.exception.InvalidProviderException;
import com.aslmk.authservice.exception.UserNotFoundException;
import com.aslmk.authservice.service.AccountService;
import com.aslmk.authservice.service.UserResolutionService;
import com.aslmk.authservice.service.impl.UserResolutionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserResolutionServiceUnitTests {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private UserResolutionServiceImpl resolutionService;

    private static final String PROVIDER_USER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";

    @Test
    void should_resolveUserId_when_accountExists() {
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = UserEntity.builder()
                .id(userId)
                .build();

        AccountEntity account = AccountEntity.builder()
                .user(userEntity)
                .build();

        Mockito.when(accountService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, ProviderName.twitch))
                .thenReturn(Optional.of(account));

        UUID actualResult = resolutionService.resolveUserId(PROVIDER_USER_ID, PROVIDER_NAME);

        Assertions.assertEquals(userId, actualResult);
    }

    @Test
    void should_throwUserNotFoundException_when_accountNotFound() {
        Mockito.when(accountService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, ProviderName.twitch))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class,
                () -> resolutionService.resolveUserId(PROVIDER_USER_ID, PROVIDER_NAME));
    }

    @Test
    void should_throwInvalidProviderException_when_providerNameInvalid() {
        Assertions.assertThrows(InvalidProviderException.class,
                () -> resolutionService.resolveUserId(PROVIDER_USER_ID, "abc"));
    }
}
