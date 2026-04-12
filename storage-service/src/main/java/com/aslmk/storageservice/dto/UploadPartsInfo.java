package com.aslmk.storageservice.dto;

import java.util.Map;

public record UploadPartsInfo(Map<Integer, String> uploadUrls,
                              Integer nextPartNumberMarker,
                              boolean hasNext) {
}
