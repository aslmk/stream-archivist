package com.aslmk.trackerservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamerDto {
    private String streamerId;
    private String streamerUsername;
    private String streamUrl;
    private String platform;

    @Override
    public String toString() {
        return "StreamerDto [streamerId=" + streamerId +
                ";\n streamerUsername=" + streamerUsername +
                ";\n streamUrl=" + streamUrl +
                ";\n platform=" + platform + "]";
    }
}
