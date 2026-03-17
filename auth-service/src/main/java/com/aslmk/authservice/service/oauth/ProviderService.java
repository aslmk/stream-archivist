package com.aslmk.authservice.service.oauth;

import com.aslmk.authservice.dto.CreateProviderDto;
import com.aslmk.authservice.domain.auth.ProviderEntity;

public interface ProviderService {
    ProviderEntity create(CreateProviderDto dto);
}
