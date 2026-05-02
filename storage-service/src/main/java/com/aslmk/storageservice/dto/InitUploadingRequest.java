package com.aslmk.storageservice.dto;

import java.util.UUID;

public record InitUploadingRequest(UUID streamId, String fileName, int expectedParts) {}
