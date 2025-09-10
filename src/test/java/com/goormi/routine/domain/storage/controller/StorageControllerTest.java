package com.goormi.routine.domain.storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormi.routine.domain.auth.filter.JwtAuthenticationFilter;
import com.goormi.routine.domain.storage.dto.PresignedUrlRequest;
import com.goormi.routine.domain.storage.dto.PresignedUrlResponse;
import com.goormi.routine.domain.storage.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageController.class)
class StorageControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private S3Service s3Service;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private PresignedUrlRequest request;
    private PresignedUrlResponse response;
    
    @BeforeEach
    void setUp() {
        request = new PresignedUrlRequest();
        request.setFileName("test.jpg");
        request.setContentType("image/jpeg");
        request.setPurpose("profile");
        
        response = PresignedUrlResponse.builder()
                .uploadUrl("https://bucket.s3.amazonaws.com/presigned-url")
                .fileUrl("https://bucket.s3.amazonaws.com/profile/test.jpg")
                .objectKey("profile/2024/12/test.jpg")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
    }
    
    @Test
    @DisplayName("Presigned URL 생성 - 성공")
    @WithMockUser(username = "1")
    void generatePresignedUrl_Success() throws Exception {
        when(s3Service.generatePresignedUrl(any(PresignedUrlRequest.class), eq(1L)))
                .thenReturn(response);
        
        mockMvc.perform(post("/api/storage/presigned-url")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").exists())
                .andExpect(jsonPath("$.data.fileUrl").exists())
                .andExpect(jsonPath("$.data.objectKey").exists());
        
        verify(s3Service, times(1)).generatePresignedUrl(any(PresignedUrlRequest.class), eq(1L));
    }
    
    @Test
    @DisplayName("Presigned URL 생성 - 잘못된 컨텐츠 타입")
    @WithMockUser(username = "1")
    void generatePresignedUrl_InvalidContentType() throws Exception {
        request.setContentType("application/pdf");
        
        mockMvc.perform(post("/api/storage/presigned-url")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        verify(s3Service, never()).generatePresignedUrl(any(), any());
    }
    
    @Test
    @DisplayName("파일 삭제 - 성공")
    @WithMockUser(username = "1")
    void deleteFile_Success() throws Exception {
        doNothing().when(s3Service).deleteFile("profile/2024/12/test.jpg");
        
        mockMvc.perform(delete("/api/storage/file")
                        .with(csrf())
                        .param("objectKey", "profile/2024/12/test.jpg"))
                .andDo(print())
                .andExpect(status().isOk());
        
        verify(s3Service, times(1)).deleteFile("profile/2024/12/test.jpg");
    }
}