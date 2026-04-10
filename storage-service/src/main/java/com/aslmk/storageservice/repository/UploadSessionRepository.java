package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadSessionRepository extends CrudRepository<UploadSessionEntity, String> {

}
