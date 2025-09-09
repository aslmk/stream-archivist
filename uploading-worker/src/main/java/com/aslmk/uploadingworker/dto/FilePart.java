package com.aslmk.uploadingworker.dto;

public record FilePart(long partNumber, long offset, long partSize) {}
