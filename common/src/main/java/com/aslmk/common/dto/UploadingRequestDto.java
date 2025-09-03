package com.aslmk.common.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadingRequestDto {
    private String streamerUsername;
    private String fileName;
    private Integer fileParts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadingRequestDto that = (UploadingRequestDto) o;
        return Objects.equals(streamerUsername, that.streamerUsername) && Objects.equals(fileName, that.fileName) && Objects.equals(fileParts, that.fileParts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamerUsername, fileName, fileParts);
    }
}
