package com.goormi.routine.domain.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Presigned URL 응답")
public class PresignedUrlResponse {
    
    @Schema(description = "업로드용 Presigned URL", example = "https://bucket.s3.amazonaws.com/...")
    private String uploadUrl;
    
    @Schema(description = "업로드 후 접근 가능한 파일 URL", example = "https://bucket.s3.amazonaws.com/files/xxx.jpg")
    private String fileUrl;
    
    @Schema(description = "S3 객체 키", example = "profile/2024/12/xxx.jpg")
    private String objectKey;
    
    @Schema(description = "URL 만료 시간")
    private LocalDateTime expiresAt;
}