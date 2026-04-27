package com.aslmk.storageservice.dto;

import java.util.UUID;

@Deprecated
public record InitChunkedUpload(UUID streamId, String filename) {}
