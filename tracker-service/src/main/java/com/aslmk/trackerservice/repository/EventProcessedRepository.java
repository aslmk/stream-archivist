package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.domain.EventProcessedEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventProcessedRepository extends CrudRepository<EventProcessedEntity, String> {}
