package com.aslmk.storageservice.dto;

import java.util.UUID;

@Deprecated
public record CompleteChunkedUpload(UUID streamId, String filename) {
}
