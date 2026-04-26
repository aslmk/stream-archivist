package com.aslmk.uploadingworker.dto;

import java.util.UUID;

public record CompleteChunkedUpload(UUID streamId, String filename) {
}
