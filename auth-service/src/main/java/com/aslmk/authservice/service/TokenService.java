package com.aslmk.authservice.service;

import com.aslmk.authservice.dto.CreateTokenDto;
import com.aslmk.authservice.entity.TokenEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenService {
    TokenEntity create(CreateTokenDto dto);
    List<TokenEntity> getExpiredTokens(LocalDateTime now);
    void update(TokenEntity entity);
}
