package com.aslmk.authservice.service.token;

import com.aslmk.authservice.domain.auth.RefreshTokenEntity;

import java.util.UUID;

public interface RefreshTokenService {
    String generate(UUID userId);
    RefreshTokenEntity validate(String token);
    void delete(String token);
}
