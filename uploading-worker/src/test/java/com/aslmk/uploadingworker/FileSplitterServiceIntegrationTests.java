package com.aslmk.uploadingworker;


import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.service.impl.FileSplitterServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileSplitterServiceIntegrationTests {

    private final FileSplitterServiceImpl service = new FileSplitterServiceImpl();

    private static final long CHUNK_SIZE = 50;

    @Test
    void should_throwFileSplittingException_when_pathIsInvalid() {
        Assertions.assertThrows(FileSplittingException.class,
                () -> service.getFileParts(Path.of("/some/file/dir"))
        );
    }

    @Test
    void should_returnValidPartsList_when_fileIsValid() throws IOException {
        long tmpFileSize = 250 * 1024 * 1024; // 250 MB
        long sizeOfChunk = CHUNK_SIZE * 1024 * 1024; // chunkSize MB
        long expectedFilePartsCount = (tmpFileSize / sizeOfChunk) + ((tmpFileSize % sizeOfChunk) > 0 ? 1 : 0);

        ReflectionTestUtils.setField(service, "chunkSize", CHUNK_SIZE);

        Path tmpFilePath = Files.createTempFile("testFile", ".txt");
        File tmpFile = tmpFilePath.toFile();

        try (RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw")) {
            raf.setLength(tmpFileSize);
        }

        Assertions.assertTrue(Files.exists(tmpFilePath));
        Assertions.assertEquals(tmpFileSize, tmpFile.length());

        List<FilePart> fileParts = service.getFileParts(tmpFilePath);

        Assertions.assertEquals(expectedFilePartsCount, fileParts.size());

        long partsSizeSum = 0;
        for (FilePart filePart : fileParts) {
            partsSizeSum += filePart.partSize();
        }
        Assertions.assertEquals(tmpFileSize, partsSizeSum);
        Assertions.assertTrue(fileParts.getLast().partSize() <= sizeOfChunk);

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

