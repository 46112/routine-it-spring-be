package com.goormi.routine.domain.storage.controller;

import com.goormi.routine.common.response.ApiResponse;
import com.goormi.routine.domain.storage.dto.PresignedUrlRequest;
import com.goormi.routine.domain.storage.dto.PresignedUrlResponse;
import com.goormi.routine.domain.storage.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "파일 업로드 관련 API")
public class StorageController {
    
    private final S3Service s3Service;
    
    @PostMapping("/presigned-url")
    @Operation(summary = "Presigned URL 생성", description = "S3 직접 업로드를 위한 Presigned URL을 생성합니다")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> generatePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        PresignedUrlResponse response = s3Service.generatePresignedUrl(request, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/file")
    @Operation(summary = "파일 삭제", description = "S3에 업로드된 파일을 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "S3 객체 키", required = true)
            @RequestParam String objectKey,
            Authentication authentication
    ) {
        s3Service.deleteFile(objectKey);
        return ResponseEntity.ok(ApiResponse.success());
    }
}