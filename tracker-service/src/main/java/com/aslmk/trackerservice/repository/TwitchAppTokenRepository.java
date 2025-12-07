package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.entity.TwitchAppTokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwitchAppTokenRepository extends CrudRepository<TwitchAppTokenEntity, UUID> {
    Optional<TwitchAppTokenEntity> findFirst();
}
