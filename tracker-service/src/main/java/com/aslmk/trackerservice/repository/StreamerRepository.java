package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.entity.StreamerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamerRepository extends CrudRepository<StreamerEntity, UUID> {
    Optional<StreamerEntity> findByUsername(String username);
    Optional<StreamerEntity> findByProviderUserIdAndProviderName(String id, String providerName);
}
