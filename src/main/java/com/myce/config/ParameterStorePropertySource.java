package com.myce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS Systems Manager Parameter Store에서 파라미터를 로드하여 
 * Spring Environment에 추가하는 컴포넌트
 * 
 * startup.sh를 대체하여 Spring Boot 애플리케이션 시작시 자동으로 파라미터 로드
 */
@Slf4j
public class ParameterStorePropertySource implements EnvironmentPostProcessor {

    /**
     * AWS Parameter Store에서 로드할 파라미터 매핑
     * Key: Spring property name, Value: AWS Parameter Store parameter name
     */
    private static final Map<String, String> PARAMETER_MAPPINGS = createParameterMappings();
    
    private static Map<String, String> createParameterMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // Database
        mappings.put("DB_URL", "/myce/db-url");
        mappings.put("DB_USERNAME", "/myce/db-username");
        mappings.put("DB_PASSWORD", "/myce/db-password");
        
        // MongoDB & Redis
        mappings.put("MONGODB_URI", "/myce/mongodb-uri");
        mappings.put("REDIS_URL", "/myce/redis-url");
        
        // Security
        mappings.put("JWT_SECRET", "/myce/jwt-secret");
        
        // AWS S3
        mappings.put("S3_MEDIA_BUCKET_NAME", "/myce/s3-bucket-name");
        mappings.put("CLOUDFRONT_DOMAIN", "/myce/cloudfront-domain");
        
        // PortOne Payment
        mappings.put("PORTONE_BASE_URL", "/myce/portone-base-url");
        mappings.put("PORTONE_API_KEY", "/myce/portone-api-key");
        mappings.put("PORTONE_API_SECRET", "/myce/portone-api-secret");
        mappings.put("PORTONE_CUSTOMER_CODE", "/myce/portone-customer-code");
        
        // Email (SES)
        mappings.put("MAIL_HOST", "/myce/ses-smtp-host");
        mappings.put("MAIL_USERNAME", "/myce/ses-smtp-username");
        mappings.put("MAIL_PASSWORD", "/myce/ses-smtp-password");
        
        // OAuth2
        mappings.put("GOOGLE_CLIENT_ID", "/myce/GOOGLE_CLIENT_ID");
        mappings.put("GOOGLE_CLIENT_SECRET", "/myce/GOOGLE_CLIENT_SECRET");
        mappings.put("KAKAO_CLIENT_ID", "/myce/KAKAO_CLIENT_ID");
        mappings.put("KAKAO_CLIENT_SECRET", "/myce/KAKAO_CLIENT_SECRET");
        
        return mappings;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String profile = System.getProperty("spring.profiles.active", 
                        System.getenv("PROFILE"));
        
        // Only load parameters for production profile
        if (!"product".equals(profile)) {
            log.info("🔧 Profile is '{}' - skipping AWS Parameter Store loading", profile);
            return;
        }

        log.info("🔐 Loading parameters from AWS Systems Manager Parameter Store...");
        
        MutablePropertySources propertySources = environment.getPropertySources();
        
        Map<String, Object> parameterMap = loadParametersFromStore();
        
        if (!parameterMap.isEmpty()) {
            // Add database driver class name
            parameterMap.put("DB_DRIVER_CLASS_NAME", "com.mysql.cj.jdbc.Driver");
            parameterMap.put("AWS_REGION", "ap-northeast-2");
            
            // Add parameters to Spring environment with high precedence
            propertySources.addFirst(new MapPropertySource("aws-parameter-store", parameterMap));
            log.info("✅ Successfully loaded {} parameters from AWS Parameter Store", parameterMap.size());
        } else {
            log.warn("⚠️ No parameters loaded from AWS Parameter Store");
        }
    }

    private Map<String, Object> loadParametersFromStore() {
        Map<String, Object> parameters = new HashMap<>();
        
        try (SsmClient ssmClient = SsmClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build()) {
            
            for (Map.Entry<String, String> entry : PARAMETER_MAPPINGS.entrySet()) {
                String propertyName = entry.getKey();
                String parameterName = entry.getValue();
                
                try {
                    GetParameterRequest request = GetParameterRequest.builder()
                            .name(parameterName)
                            .withDecryption(true) // Decrypt SecureString parameters
                            .build();
                    
                    GetParameterResponse response = ssmClient.getParameter(request);
                    String value = response.parameter().value();
                    
                    parameters.put(propertyName, value);
                    log.debug("🔑 Loaded parameter: {}", propertyName);
                    
                } catch (ParameterNotFoundException e) {
                    log.warn("⚠️ Parameter not found: {} ({})", propertyName, parameterName);
                } catch (Exception e) {
                    log.error("❌ Failed to load parameter: {} - {}", propertyName, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize SSM client: {}", e.getMessage());
        }
        
        return parameters;
    }
}