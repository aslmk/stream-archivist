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

    @Value("${user.file.part-size}")
    private long FILE_PART_SIZE;

    @Override
    public Map<Integer, FilePartData> getFileParts(Path filePath) {
        log.info("Starting file splitting: path='{}'", filePath);

        Map<Integer, FilePartData> parts = new HashMap<>();

        try {
            long fileSize = Files.size(filePath);
            log.debug("File size: '{}' bytes", fileSize);

            if (fileSize <= 0) {
                log.error("Cannot split file: file is empty");
                throw new FileSplittingException("File is empty");
            }

            long filePartSize = FILE_PART_SIZE * 1024 * 1024;
            long partsCount = (fileSize / filePartSize) + ((fileSize % filePartSize) > 0 ? 1 : 0);
            long offset = 0;

            for (int partNumber = 1; partNumber <= partsCount; partNumber++) {
                FilePartData partData = new FilePartData(offset, Math.min(filePartSize, fileSize-offset));
                offset += filePartSize;
                parts.put(partNumber, partData);
            }

        } catch (IOException e) {
            throw new FileSplittingException("Failed to split file: " + e.getMessage());
        }

        log.info("File successfully split: part(s)='{}', filePath='{}'", parts.size(), filePath);
        return parts;
    }
}
