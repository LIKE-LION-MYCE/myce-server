package com.myce.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * 이미지 업로드 Controller
 * <p>
 * S3 Presigned URL을 생성하여 클라이언트가 직접 S3에 이미지를 업로드할 수 있게 합니다.
 * AwsConfig에서 제공하는 통합 인증을 통해 생성된 S3Client와 S3Presigner를 사용합니다.
 *
 * @see com.myce.common.config.AwsConfig 통합 AWS 인증 설정
 * @see com.myce.common.config.S3Config S3 클라이언트 설정
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    // S3Config에서 AwsConfig의 통합 인증을 사용하여 생성된 Bean들을 주입
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket.media}")
    private String bucketName;

    @Value("${cloudfront.domain:https://media.myce.live}")
    private String cloudfrontDomain;

    /**
     * S3 Presigned URL 생성 API
     * <p>
     * 클라이언트가 직접 S3에 이미지를 업로드할 수 있는 임시 URL을 생성합니다.
     * <p>
     * 인증: @PreAuthorize("permitAll()") 제거됨
     * - 이유: SecurityConfig에서 /api/images/** 경로를 permitAll()로 설정했으므로 중복
     * - 일관성: 다른 컨트롤러들과 동일한 패턴 유지
     * <p>
     * CORS: @CrossOrigin 제거됨
     * - 이유: CorsConfig에서 전역 CORS 설정이 이미 모든 필요한 도메인을 포함
     * - 문제점: 컨트롤러별 CORS 설정은 전역 설정과 충돌 가능성 존재
     *
     * @param filename 업로드할 파일명 (확장자 추출용)
     * @return Presigned URL과 CDN URL이 포함된 응답
     */
    @GetMapping("/presign")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String filename) {

        try {
            // 1. 고유 파일명 생성 (UUID + 원본 확장자)
            String key = "images/" + UUID.randomUUID() + getFileExtension(filename);

            // 2. S3 업로드 요청 객체 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // 3. Presigned URL 생성 (15분 유효)
            // AwsConfig의 통합 인증을 통해 생성된 S3Presigner 사용
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .putObjectRequest(putObjectRequest)
                    .build();

            String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

            // 4. CloudFront CDN URL 생성 (업로드 완료 후 접근용)
            String cdnUrl = cloudfrontDomain + "/" + key;

            // 5. 클라이언트 응답 생성
            Map<String, String> response = Map.of(
                    "uploadUrl", uploadUrl,  // 클라이언트가 PUT 요청할 URL
                    "cdnUrl", cdnUrl        // 업로드 완료 후 이미지 접근 URL
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Presigned URL 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 파일명에서 확장자 추출
     *
     * @param filename 원본 파일명
     * @return 확장자 (점 포함, 예: ".jpg") 또는 빈 문자열
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        return lastDot != -1 ? filename.substring(lastDot) : "";
    }
}
