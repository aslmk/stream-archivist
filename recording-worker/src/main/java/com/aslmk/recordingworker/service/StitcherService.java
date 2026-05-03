package com.aslmk.recordingworker.service;

public interface StitcherService {
    void init(String key);
    void append(String key, String data);
    boolean stitch(String key, String fileOutputName);
    void clearStitchedParts(String key);
}
