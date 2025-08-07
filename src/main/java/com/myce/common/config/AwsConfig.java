package com.myce.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;

/**
 * AWS 인증 중앙 관리 Configuration
 * <p>
 * 이 클래스는 모든 AWS 서비스가 사용할 통합 인증 제공자를 관리한다.
 * 로컬 개발환경과 프로덕션 환경에서 자동으로 적절한 인증 방식을 선택한다.
 * <p>
 * 인증 우선순위:
 * 1. Spring Properties (.env 파일의 AWS 자격증명) - 로컬 개발
 * 2. DefaultCredentialsProvider (환경변수, AWS CLI, IAM 역할) - 프로덕션
 * <p>
 * 사용되는 AWS 서비스:
 * - S3Client (파일 업로드/다운로드)
 * - S3Presigner (Presigned URL 생성)
 * - 향후 추가될 모든 AWS 서비스 (SES, SNS, Lambda 등)
 * <p>
 * 설계 이유:
 * - 팀원들이 AWS CLI 설정을 배울 필요 없음
 * - .env 파일만으로 로컬 개발 가능
 * - 프로덕션에서는 IAM 역할 자동 사용
 * - 단일 설정으로 모든 환경 지원
 *
 * @author 데브옵스 Team ㅋㅋㅋ
 * @since 2025-08-07
 */
@Configuration
@Slf4j
public class AwsConfig {

    @Value("${spring.cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key:}")
    private String secretKey;

    /**
     * AWS 인증 제공자 - 모든 AWS 서비스가 사용하는 중앙 인증 소스
     * 로컬 개발환경: .env 파일의 AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY 사용
     * 프로덕션 환경: EC2 IAM 역할 자동 사용
     *
     * @return AwsCredentialsProvider 통합 인증 제공자
     */
    @Bean
    @Primary
    public AwsCredentialsProvider awsCredentialsProvider() {
        log.info("AWS 인증 제공자 초기화 중...");

        return AwsCredentialsProviderChain.of(
                // 1순위: Spring Properties에서 AWS 자격증명 확인 (로컬 개발)
                () -> {
                    if (accessKey != null && !accessKey.trim().isEmpty() &&
                            secretKey != null && !secretKey.trim().isEmpty()) {
                        log.info("로컬 개발 모드: .env 파일의 AWS 자격증명 사용");
                        return AwsBasicCredentials.create(accessKey, secretKey);
                    }
                    log.debug("Spring Properties에서 AWS 자격증명을 찾을 수 없음");
                    throw SdkClientException.create("No credentials in Spring properties");
                },

                // 2순위: 표준 AWS 자격증명 체인 (프로덕션)
                DefaultCredentialsProvider.create()
        );
    }
}