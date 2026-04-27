package com.aslmk.uploadingworker.dto;

import java.util.UUID;

@Deprecated
public record CompleteChunkedUpload(UUID streamId, String filename) {
}
