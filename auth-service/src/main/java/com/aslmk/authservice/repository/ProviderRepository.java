package com.aslmk.authservice.repository;

import com.aslmk.authservice.entity.ProviderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProviderRepository extends CrudRepository<ProviderEntity, UUID> {
}
