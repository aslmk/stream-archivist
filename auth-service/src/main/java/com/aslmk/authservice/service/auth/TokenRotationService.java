package com.aslmk.authservice.service.auth;

import com.aslmk.authservice.dto.JwtTokenPair;

public interface TokenRotationService {

    JwtTokenPair refreshTokens(String refreshToken);

}
