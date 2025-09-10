package com.goormi.routine.domain.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Presigned URL 요청")
public class PresignedUrlRequest {
    
    @NotBlank(message = "파일 이름은 필수입니다")
    @Schema(description = "업로드할 파일 이름", example = "profile.jpg")
    private String fileName;
    
    @NotBlank(message = "컨텐츠 타입은 필수입니다")
    @Pattern(regexp = "^image/(jpeg|jpg|png|gif|webp)$", message = "이미지 파일만 업로드 가능합니다")
    @Schema(description = "파일의 MIME 타입", example = "image/jpeg")
    private String contentType;
    
    @Schema(description = "파일 용도", example = "profile", allowableValues = {"profile", "proof-shot", "group-image"})
    private String purpose = "profile";
}