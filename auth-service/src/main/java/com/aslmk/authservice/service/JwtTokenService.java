package com.aslmk.authservice.service;

public interface JwtTokenService {
    String generate(String providerUserId, String providerName);
}
