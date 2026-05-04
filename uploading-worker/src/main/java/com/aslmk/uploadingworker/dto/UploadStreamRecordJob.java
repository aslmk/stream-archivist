package com.aslmk.uploadingworker.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadStreamRecordJob {
    private String filename;
    private UUID streamId;
}
