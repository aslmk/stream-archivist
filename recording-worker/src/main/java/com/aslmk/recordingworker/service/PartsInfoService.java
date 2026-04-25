package com.aslmk.recordingworker.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface PartsInfoService {
    long getRecordedPartIndex(String recordedPartName);
    Optional<String> watchForNewRecordedPart(String key);
    Optional<String> getLastRecordedPartName(Path path);
    String getFilePartPath(String key);
    Path getPartsInfoPath(String key);
    boolean isPartsInfoExists(String key);
    List<String> getRecordedParts(String key);
    void clearPendingFileParts(String key);
}
