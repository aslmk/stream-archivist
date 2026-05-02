package com.aslmk.uploadingworker.dto;

import java.util.UUID;

public record InitUploadingRequest(UUID streamId, String fileName, int expectedParts) {}
