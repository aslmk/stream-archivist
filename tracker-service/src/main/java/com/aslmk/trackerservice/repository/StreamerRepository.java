package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.entity.Streamer;
import com.aslmk.trackerservice.entity.StreamingPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamerRepository extends JpaRepository<Streamer, UUID> {
    Optional<Streamer> findByUsernameAndPlatform(String username, StreamingPlatform platform);
}
