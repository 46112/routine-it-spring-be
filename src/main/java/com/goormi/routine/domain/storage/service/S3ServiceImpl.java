package com.goormi.routine.domain.storage.service;

import com.goormi.routine.domain.storage.dto.PresignedUrlRequest;
import com.goormi.routine.domain.storage.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${aws.s3.bucket:routineit-storage}")
    private String bucketName;
    
    @Value("${aws.s3.presigned-url-duration:15}")
    private int presignedUrlDurationMinutes;
    
    @Value("${aws.cloudfront.domain:}")
    private String cloudfrontDomain;
    
    @Override
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request, Long userId) {
        String objectKey = generateObjectKey(request.getPurpose(), request.getFileName(), userId);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(request.getContentType())
                .build();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlDurationMinutes))
                .putObjectRequest(putObjectRequest)
                .build();
        
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String uploadUrl = presignedRequest.url().toString();
        
        String fileUrl = generateFileUrl(objectKey);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(presignedUrlDurationMinutes);
        
        log.info("Generated presigned URL for user: {}, object: {}", userId, objectKey);
        
        return PresignedUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .fileUrl(fileUrl)
                .objectKey(objectKey)
                .expiresAt(expiresAt)
                .build();
    }
    
    @Override
    public void deleteFile(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
            log.info("Deleted S3 object: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete S3 object: {}", objectKey, e);
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }
    
    private String generateObjectKey(String purpose, String fileName, Long userId) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        return String.format("%s/%s/user-%d/%s", purpose, date, userId, uniqueFileName);
    }
    
    private String generateFileUrl(String objectKey) {
        if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
            return String.format("https://%s/%s", cloudfrontDomain, objectKey);
        }
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, objectKey);
    }
}