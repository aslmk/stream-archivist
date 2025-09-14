package com.aslmk.uploadingworker.service;

import com.aslmk.common.dto.RecordCompletedEvent;

public interface StreamUploaderService {
    void processUploadingRequest(RecordCompletedEvent recordCompletedEvent);
}
