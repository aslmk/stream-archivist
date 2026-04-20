package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.repository.RecordedFilePartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecordedFilePartServiceImpl implements RecordedFilePartService {

    private final RecordedFilePartRepository repository;

    public RecordedFilePartServiceImpl(RecordedFilePartRepository repository) {
        this.repository = repository;
    }


}
