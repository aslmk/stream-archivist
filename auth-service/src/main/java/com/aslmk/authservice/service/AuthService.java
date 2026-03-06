package com.aslmk.authservice.service;

import com.aslmk.authservice.dto.JwtTokenPair;

public interface AuthService {

    JwtTokenPair refreshTokens(String refreshToken);

}
