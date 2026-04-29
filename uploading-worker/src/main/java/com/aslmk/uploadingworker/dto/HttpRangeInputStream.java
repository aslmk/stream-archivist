package com.aslmk.uploadingworker.dto;

import java.io.*;

public class HttpRangeInputStream extends InputStream {
    private long remaining;
    private final RandomAccessFile raf;

    public HttpRangeInputStream(File file, long offset, long partSize) throws IOException {
        if (!file.exists()) throw new FileNotFoundException("File not found: " + file.getPath());

        this.raf = new RandomAccessFile(file, "r");

        if (offset > this.raf.length()) {
            this.raf.close();
            throw new IOException("Offset " + offset + " is beyond file length " + this.raf.length());
        }

        this.raf.seek(offset);
        this.remaining = partSize;
    }

    @Override
    public int read() throws IOException {
        if (remaining <= 0) return -1;

        int data = this.raf.read();

        if (data != -1) {
            remaining--;
        }

        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (remaining <= 0) return -1;

        int amountToRead = (int)Math.min(len, remaining);
        int data = raf.read(b, off, amountToRead);

        if (data > 0) remaining-=data;
        return data;
    }

    @Override
    public void close() throws IOException {
        this.raf.close();
    }
}
