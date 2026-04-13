package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.FilePartData;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FileSplitterServiceImpl implements FileSplitterService {

    @Value("${user.file.chunk-size}")
    private long chunkSize;

    @Override
    public Map<Integer, FilePartData> getFileParts(Path filePath) {
        log.info("Starting file splitting: path='{}'", filePath);

        Map<Integer, FilePartData> parts = new HashMap<>();

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

            for (int partNumber = 1; partNumber <= partsCount; partNumber++) {
                FilePartData partData = new FilePartData(offset, Math.min(sizeOfChunk, fileSize-offset));
                offset += sizeOfChunk;
                parts.put(partNumber, partData);
            }

        } catch (IOException e) {
            log.error("Failed to read file '{}'", filePath, e);
            throw new FileSplittingException("Error while getting file parts: " + e.getMessage());
        }

        log.info("File successfully split into {} part(s) for '{}'", parts.size(), filePath);
        return parts;
    }
}
