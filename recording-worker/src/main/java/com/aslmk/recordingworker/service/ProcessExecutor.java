package com.aslmk.recordingworker.service;

import java.util.List;

public interface ProcessExecutor {

    int execute(List<String> command);
}
