package com.aslmk.common.dto;

import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadingResponseDto {
    private String uploadId;
    private List<String> uploadURLs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadingResponseDto that = (UploadingResponseDto) o;
        return Objects.equals(uploadId, that.uploadId) && Objects.equals(uploadURLs, that.uploadURLs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uploadId, uploadURLs);
    }
}
