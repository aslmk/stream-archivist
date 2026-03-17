package com.aslmk.authservice.repository;

import com.aslmk.authservice.domain.auth.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    void deleteByTokenHash(String tokenHash);
}
