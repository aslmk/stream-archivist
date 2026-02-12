package com.aslmk.authservice.service;

import java.util.UUID;

public interface JwtTokenService {
    String generate(UUID userId);
}
