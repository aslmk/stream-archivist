package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.FilePart;

import java.util.List;

public interface FileSplitterService {
    List<FilePart> getFileParts(String fileName);
}
