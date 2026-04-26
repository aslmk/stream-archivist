package com.aslmk.storageservice.dto;

import java.util.UUID;

public record StreamSessionData(UUID streamId, String uploadId, String key) {
}
