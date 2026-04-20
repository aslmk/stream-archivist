package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.dto.RecordedPartDto;

public interface RecordedFilePartService {
    boolean saveIfNotExist(RecordedPartDto dto);
}
