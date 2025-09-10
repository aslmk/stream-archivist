package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.FilePart;

import java.nio.file.Path;
import java.util.List;

public interface FileSplitterService {
    List<FilePart> getFileParts(Path filePath);
}
