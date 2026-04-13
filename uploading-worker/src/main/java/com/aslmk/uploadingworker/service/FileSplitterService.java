package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.FilePartData;

import java.nio.file.Path;
import java.util.Map;

public interface FileSplitterService {
    Map<Integer, FilePartData> getFileParts(Path filePath);
}
