package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.RecordedPartEvent;
import com.aslmk.uploadingworker.dto.RecordingStatusEvent;

public interface StreamUploaderService {
    void processUploadingRequest(RecordingStatusEvent event);
    void processChunkedUploadingRequest(RecordedPartEvent event);
}
