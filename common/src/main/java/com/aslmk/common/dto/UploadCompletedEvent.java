package com.aslmk.common.dto;


import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadCompletedEvent {
    private List<PartUploadResultDto> partUploadResults;
    private String filename;
    private String uploadId;
    private String streamerUsername;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadCompletedEvent that = (UploadCompletedEvent) o;
        return Objects.equals(partUploadResults, that.partUploadResults) && Objects.equals(filename, that.filename) && Objects.equals(uploadId, that.uploadId) && Objects.equals(streamerUsername, that.streamerUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partUploadResults, filename, uploadId, streamerUsername);
    }
}
