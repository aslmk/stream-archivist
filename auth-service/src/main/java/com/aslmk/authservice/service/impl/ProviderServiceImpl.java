package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.dto.CreateProviderDto;
import com.aslmk.authservice.entity.ProviderEntity;
import com.aslmk.authservice.repository.ProviderRepository;
import com.aslmk.authservice.service.ProviderService;
import org.springframework.stereotype.Service;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderServiceImpl(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    public ProviderEntity create(CreateProviderDto dto) {
        ProviderEntity providerEntity = ProviderEntity.builder()
                .providerName(dto.getProviderName())
                .providerUserId(dto.getProviderUserId())
                .user(dto.getUser())
                .build();

        return providerRepository.save(providerEntity);
    }
}
