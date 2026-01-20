package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.dto.CreateStreamerDto;
import com.aslmk.trackerservice.entity.StreamerEntity;

import java.util.Optional;
import java.util.UUID;

public interface StreamerService {
    Optional<StreamerEntity> findByUsername(String username);
    Optional<StreamerEntity> findByProviderUserIdAndProviderName(String id, String providerName);
    UUID create(CreateStreamerDto dto);
    void updateUsername(StreamerEntity entity, String username);
    void updateStatus(StreamerEntity entity, boolean isOnline);
}
