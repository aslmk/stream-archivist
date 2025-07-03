package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.entity.Broadcaster;
import com.aslmk.trackerservice.entity.StreamingPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BroadcasterRepository extends JpaRepository<Broadcaster, UUID> {
    Optional<Broadcaster> findByUsernameAndPlatform(String username, StreamingPlatform platform);
}
