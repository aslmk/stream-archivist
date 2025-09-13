package com.aslmk.uploadingworker.service;

public interface StreamUploaderService {
    void processUploadingRequest(String streamerUsername, String fileName);
}
