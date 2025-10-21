package com.aslmk.authservice.repository;

import com.aslmk.authservice.entity.TokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TokenRepository extends CrudRepository<TokenEntity, UUID> {
    List<TokenEntity> findAllByExpiresAtBefore(LocalDateTime now);
}
