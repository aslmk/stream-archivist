package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.PartUploadResultDto;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;

import java.util.List;

public interface S3UploaderService {

    List<PartUploadResultDto> upload(S3UploadRequestDto request);
}
