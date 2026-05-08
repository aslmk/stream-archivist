package com.aslmk.storageservice.dto;

import java.util.UUID;

public record CompleteUploadingRequest(UUID streamId, String fileName) {}
