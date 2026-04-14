package com.aslmk.uploadingworker;


import com.aslmk.uploadingworker.dto.FilePartData;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.service.FileSplitterServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileSplitterServiceTests {
    private final FileSplitterServiceImpl service = new FileSplitterServiceImpl();
    private static final long FILE_PART_SIZE = 50;

    @Test
    void should_throwFileSplittingException_when_pathIsInvalid() {
        Assertions.assertThrows(FileSplittingException.class,
                () -> service.getFileParts(Path.of("/some/file/dir"))
        );
    }

    @Test
    void should_returnValidPartsMap_when_fileIsValid() throws IOException {
        long tmpFileSize = 250 * 1024 * 1024; // 250 MB
        long filePartSize = FILE_PART_SIZE * 1024 * 1024;
        long expectedFilePartsCount = (tmpFileSize / filePartSize) + ((tmpFileSize % filePartSize) > 0 ? 1 : 0);
        ReflectionTestUtils.setField(service, "FILE_PART_SIZE", FILE_PART_SIZE);
        Path tmpFilePath = Files.createTempFile("testFile", ".txt");
        File tmpFile = tmpFilePath.toFile();
        try (RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw")) {
            raf.setLength(tmpFileSize);
        }
        Assertions.assertTrue(Files.exists(tmpFilePath));
        Assertions.assertEquals(tmpFileSize, tmpFile.length());

        Map<Integer, FilePartData> fileParts = service.getFileParts(tmpFilePath);

        Assertions.assertEquals(expectedFilePartsCount, fileParts.size());

        long partsSizeSum = 0;
        for (FilePartData partData : fileParts.values()) {
            partsSizeSum += partData.partSize();
        }
        Assertions.assertEquals(tmpFileSize, partsSizeSum);

        FilePartData lastPart = fileParts.get((int) expectedFilePartsCount);
        Assertions.assertNotNull(lastPart);
        Assertions.assertTrue(lastPart.partSize() <= filePartSize);

        tmpFile.deleteOnExit();
    }

    @Test
    void should_throwFileSplittingException_when_fileSizeIsZero() throws IOException {
        long tmpFileSize = 0;
        Path tmpFilePath = Files.createTempFile("testFile", ".txt");
        File tmpFile = tmpFilePath.toFile();
        try (RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw")) {
            raf.setLength(tmpFileSize);
        }
        Assertions.assertTrue(Files.exists(tmpFilePath));
        Assertions.assertEquals(tmpFileSize, tmpFile.length());
        Assertions.assertThrows(FileSplittingException.class, () -> service.getFileParts(tmpFilePath));
        tmpFile.deleteOnExit();
    }
}