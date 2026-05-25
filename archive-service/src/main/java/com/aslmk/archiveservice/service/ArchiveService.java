package com.aslmk.archiveservice.service;

import com.aslmk.archiveservice.dto.StreamRecordings;

import java.util.UUID;

public interface ArchiveService {
    StreamRecordings getStreamRecordings(UUID streamerId);
}
