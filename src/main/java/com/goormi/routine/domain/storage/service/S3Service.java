package com.goormi.routine.domain.storage.service;

import com.goormi.routine.domain.storage.dto.PresignedUrlRequest;
import com.goormi.routine.domain.storage.dto.PresignedUrlResponse;

public interface S3Service {
    PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request, Long userId);
    void deleteFile(String objectKey);
}