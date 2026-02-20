package com.aslmk.uploadingworker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "user.storage.recordings")
public class RecordingStorageProperties {
    private String path;
}
