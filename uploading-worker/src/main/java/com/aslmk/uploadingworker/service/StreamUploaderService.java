package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.RecordingStatusEvent;

public interface StreamUploaderService {
    void processUploadingRequest(RecordingStatusEvent event);
}
