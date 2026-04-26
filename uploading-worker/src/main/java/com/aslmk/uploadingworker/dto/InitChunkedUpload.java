package com.aslmk.uploadingworker.dto;

import java.util.UUID;

public record InitChunkedUpload(UUID streamId, String filename) {}
