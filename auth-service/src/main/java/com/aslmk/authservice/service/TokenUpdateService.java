package com.aslmk.authservice.service;

import com.aslmk.authservice.entity.TokenEntity;

public interface TokenUpdateService {
    void updateExpiredTokens();
    void updateIfExpired(TokenEntity token);
}
