package com.myce.common.service;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket.media}")
    private String bucketName;

    @Value("${spring.cloud.aws.s3.bucket.cloudfront-domain}")
    private String cloudfrontDomain;


    public String uploadFile(byte[] fileData, String key, String contentType) {
        log.info("S3 파일 업로드 시작 - 키: {}, 크기: {} bytes", key, fileData.length);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));
            
            String cdnUrl = cloudfrontDomain + "/" + key;
            log.info("S3 파일 업로드 완료 - CDN URL: {}", cdnUrl);
            
            return cdnUrl;
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패 - 키: {}, 오류: {}", key, e.getMessage(), e);
            throw new CustomException(CustomErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public String uploadQrImage(byte[] qrImageData, String token) {
        String key = "qr-codes/" + token + ".png";
        return uploadFile(qrImageData, key, "image/png");
    }
}