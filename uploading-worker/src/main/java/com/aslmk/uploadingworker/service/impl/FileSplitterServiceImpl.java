package com.aslmk.uploadingworker.service.impl;

import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.service.FileSplitterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileSplitterServiceImpl implements FileSplitterService {

    @Value("${user.file.save-directory}")
    private String saveDirectory;

    @Value("${user.file.chunk-size}")
    private long chunkSize;

    private static final String RECORDINGS_DIR = "recordings";

    @Override
    public List<FilePart> getFileParts(String fileName) {
        String filePath = getFilePath(fileName);
        Path path = Paths.get(filePath);
        List<FilePart> partsList = new ArrayList<>();

        try {
            long fileSize = Files.size(path);
            long sizeOfChunk = chunkSize * 1024 * 1024;
            long partsCount = (fileSize / sizeOfChunk) + ((fileSize % sizeOfChunk) > 0 ? 1 : 0);
            long offset = 0;

            for (int i = 1; i <= partsCount; i++) {
                FilePart part = new FilePart(i, offset, Math.min(sizeOfChunk, fileSize-offset));
                offset += sizeOfChunk;
                partsList.add(part);
            }

        } catch (IOException e) {
            throw new FileSplittingException("Error while getting file parts: " + e.getMessage());
        }

        return partsList;
    }

    private String getFilePath(String fileName) {
        Path currentDir = Paths.get("").toAbsolutePath();
        Path projectRoot = currentDir.getParent();
        String filePath = projectRoot.resolve(saveDirectory).resolve(RECORDINGS_DIR).toString();
        return filePath + "/" + fileName;
    }
}
