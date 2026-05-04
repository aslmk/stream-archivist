package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.UploadStreamRecordJob;

public interface StreamUploaderService {
    void processUploadingJob(UploadStreamRecordJob job);
}
