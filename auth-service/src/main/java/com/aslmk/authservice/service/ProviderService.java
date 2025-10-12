package com.aslmk.authservice.service;

import com.aslmk.authservice.dto.CreateProviderDto;
import com.aslmk.authservice.entity.ProviderEntity;

public interface ProviderService {
    ProviderEntity create(CreateProviderDto dto);
}
