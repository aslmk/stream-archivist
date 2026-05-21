package com.aslmk.storageservice.dto;

import java.util.UUID;

public record UploadingSessionData(UUID streamId,
                                   String objectKey,
                                   String uploadId,
                                   int expectedParts) {}
