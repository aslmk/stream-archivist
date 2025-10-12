package com.aslmk.authservice.repository;

import com.aslmk.authservice.entity.AccountEntity;
import com.aslmk.authservice.entity.ProviderName;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends CrudRepository<AccountEntity, UUID> {
    @Query(value = """
    SELECT * FROM accounts
    WHERE provider_user_id = :providerUserId
      AND provider_name::text = :#{#providerName.name()}
    """, nativeQuery = true)
    Optional<AccountEntity> findByProviderUserIdAndProviderName(
            String providerUserId,
            ProviderName providerName);
}

