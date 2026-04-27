package com.aslmk.uploadingworker.dto;

import java.util.UUID;

@Deprecated
public record InitChunkedUpload(UUID streamId, String filename) {}
