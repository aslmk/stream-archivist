package com.aslmk.authservice.service;

import com.aslmk.authservice.dto.CreateTokenDto;
import com.aslmk.authservice.entity.TokenEntity;

public interface TokenService {
    TokenEntity create(CreateTokenDto dto);
}
