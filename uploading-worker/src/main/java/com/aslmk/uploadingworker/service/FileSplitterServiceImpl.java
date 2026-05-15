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

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class FileSplitterServiceImpl implements FileSplitterService {

    @Value("${user.file.part-size}")
    private long FILE_PART_SIZE;

    @Override
    public Map<Integer, FilePartData> getFileParts(Path filePath) {
        log.debug("Starting file splitting",
                kv("file", filePath));

        Map<Integer, FilePartData> parts = new HashMap<>();

        try {
            long fileSize = Files.size(filePath);

            if (fileSize <= 0) {
                throw new FileSplittingException("File is empty: file=" + filePath);
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
            throw new FileSplittingException(String.format(
                    "Failed to split file: file='%s', error='%s'", filePath, e.getMessage()));
        }

        log.debug("File split",
                kv("file", filePath),
                kv("parts", parts.size()));
        return parts;
    }
}
