package com.myce.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ImageController {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket.media}")
    private String bucketName;


    @Value("${cloudfront.domain:https://media.myce.live}")

    private String cloudfrontDomain;

    @GetMapping("/presign")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String filename) {
        
        try {
            // 1. 고유 파일명 생성
            String key = "images/" + UUID.randomUUID() + getFileExtension(filename);
            
            // 2. Presigned URL 생성 (15분 유효)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .putObjectRequest(putObjectRequest)
                    .build();

            String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
            
            // 3. CloudFront CDN URL 생성
            String cdnUrl = cloudfrontDomain + "/" + key;
            
            // 4. 응답 반환
            Map<String, String> response = Map.of(
                "uploadUrl", uploadUrl,
                "cdnUrl", cdnUrl
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Presigned URL 생성 실패: " + e.getMessage()));
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        return lastDot != -1 ? filename.substring(lastDot) : "";
    }
}
