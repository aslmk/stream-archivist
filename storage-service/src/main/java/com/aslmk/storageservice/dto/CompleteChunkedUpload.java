package com.aslmk.storageservice.dto;

import java.util.UUID;

public record CompleteChunkedUpload(UUID streamId, String filename) {
}
