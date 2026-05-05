package com.aslmk.recordingorchestratorservice.domain;

import com.aslmk.recordingorchestratorservice.dto.RecordStreamJob;
import com.aslmk.recordingorchestratorservice.dto.UploadStreamRecordJob;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RecordStreamJob.class, name = "record"),
        @JsonSubTypes.Type(value = UploadStreamRecordJob.class, name = "upload")
})
public interface JobPayload {}
