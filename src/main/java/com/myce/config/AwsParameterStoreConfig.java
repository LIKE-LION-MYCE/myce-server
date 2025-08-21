package com.myce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * AWS Systems Manager Parameter Store 설정
 * EC2 IAM Role을 사용하여 SSM Parameter Store에 접근
 */
@Slf4j
@Configuration
public class AwsParameterStoreConfig {

    @Bean
    public SsmClient ssmClient() {
        log.info("🔧 Initializing AWS SSM Client with EC2 IAM role credentials");
        return SsmClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build(); // EC2 IAM role automatically used
    }
}