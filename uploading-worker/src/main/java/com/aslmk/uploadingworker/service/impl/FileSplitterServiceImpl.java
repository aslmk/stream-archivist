package com.aslmk.uploadingworker.service.impl;

import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.service.FileSplitterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FileSplitterServiceImpl implements FileSplitterService {

    @Value("${user.file.chunk-size}")
    private long chunkSize;

    @Override
    public List<FilePart> getFileParts(Path filePath) {
        log.info("Starting file splitting: path='{}'", filePath);

        List<FilePart> partsList = new ArrayList<>();

        try {
            long fileSize = Files.size(filePath);
            log.debug("File size: {} bytes", fileSize);

            if (fileSize <= 0) {
                log.error("Cannot split file: file is empty");
                throw new FileSplittingException("File is empty");
            }

            long sizeOfChunk = chunkSize * 1024 * 1024;
            long partsCount = (fileSize / sizeOfChunk) + ((fileSize % sizeOfChunk) > 0 ? 1 : 0);
            long offset = 0;

            log.info("Splitting file into {} part(s). Chunk size = {} bytes", partsCount, sizeOfChunk);

            for (int i = 1; i <= partsCount; i++) {
                FilePart part = new FilePart(i, offset, Math.min(sizeOfChunk, fileSize-offset));
                offset += sizeOfChunk;
                partsList.add(part);
            }

        } catch (IOException e) {
            log.error("Failed to read file '{}'", filePath, e);
            throw new FileSplittingException("Error while getting file parts: " + e.getMessage());
        }

        log.info("File successfully split into {} part(s) for '{}'", partsList.size(), filePath);
        return partsList;
    }
}
