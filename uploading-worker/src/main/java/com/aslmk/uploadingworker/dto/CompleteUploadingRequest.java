package com.aslmk.uploadingworker.dto;

import java.util.UUID;

public record CompleteUploadingRequest(UUID streamId, String fileName) {}
