package com.aslmk.authservice.service.token;

import com.aslmk.authservice.domain.auth.TokenEntity;

public interface TokenUpdateService {
    void updateExpiredTokens();
    void updateIfExpired(TokenEntity token);
}
