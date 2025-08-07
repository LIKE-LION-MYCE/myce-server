package com.myce.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 서비스 Configuration
 * <p>
 * AWS S3 클라이언트와 Presigner를 설정합니다.
 * AwsConfig에서 제공하는 통합 인증 제공자를 사용합니다.
 *
 * @see AwsConfig AWS 인증 설정
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    private final AwsCredentialsProvider awsCredentialsProvider;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
