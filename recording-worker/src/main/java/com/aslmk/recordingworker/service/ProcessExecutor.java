package com.aslmk.recordingworker.service;

import java.util.List;

public interface ProcessExecutor {

    boolean execute(List<String> command);
}
