package com.aslmk.recordingworker.service;

public interface StitcherService {
    void init(String key);
    void append(String key, String data);
    void stitch(String key, String fileOutputName);
}
