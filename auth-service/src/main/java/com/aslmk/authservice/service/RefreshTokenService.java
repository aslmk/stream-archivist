package com.aslmk.authservice.service;

import com.aslmk.authservice.entity.RefreshTokenEntity;

import java.util.UUID;

public interface RefreshTokenService {
    String generate(UUID userId);
    RefreshTokenEntity validate(String token);
    void delete(String token);
}
