package com.aslmk.authservice.service.token;

import java.util.UUID;

public interface JwtTokenService {
    String generate(UUID userId);
}
