package com.aslmk.storageservice.service;

import com.aslmk.storageservice.repository.UploadSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class UploadSessionServiceImpl implements UploadSessionService {
    private final UploadSessionRepository repository;

    public UploadSessionServiceImpl(UploadSessionRepository repository) {
        this.repository = repository;
    }
}
