package com.myce.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "password", "token", "secret", "authorization", "cookie", "refreshToken", "accessToken"
    );
    
    private static final Set<String> EXCLUDED_METHODS = Set.of(
        "health", "actuator", "metrics", "prometheus"
    );
    
    private static final int MAX_RESPONSE_LENGTH = 1000;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private Environment environment;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!shouldLog(joinPoint)) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String apiInfo = getApiInfo();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            logSuccessResponse(apiInfo, result, executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logErrorResponse(apiInfo, e, executionTime);
            throw e;
        }
    }
    
    private boolean shouldLog(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        String className = joinPoint.getTarget().getClass().getSimpleName().toLowerCase();
        
        if (EXCLUDED_METHODS.stream().anyMatch(excluded -> 
            methodName.contains(excluded) || className.contains(excluded))) {
            return false;
        }
        
        if (!log.isInfoEnabled() && !log.isDebugEnabled()) {
            return false;
        }

        // 배포 환경에서 Get 로깅은 Debug일때만으로 제한 - 로급 볼륨 조절 때문
        if (isProductionEnvironment() && isGetRequest(joinPoint)) {
            return log.isDebugEnabled();
        }
        
        return true;
    }
    
    private void logSuccessResponse(String apiInfo, Object result, long executionTime) {
        try {
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> response = (ResponseEntity<?>) result;
                Object body = response.getBody();
                
                String sanitizedBody = sanitizeAndTruncateResponse(body);
                
                if (executionTime > 3000) {
                    log.warn("[API-SLOW] {} - {}ms - Status: {} - Body: {}",
                            apiInfo, executionTime, response.getStatusCode(), sanitizedBody);
                } else {
                    log.info("[API-SUCCESS] {} - {}ms - Status: {} - Body: {}",
                            apiInfo, executionTime, response.getStatusCode(), sanitizedBody);
                }
            } else {
                String sanitizedResult = sanitizeAndTruncateResponse(result);
                log.info("[API-SUCCESS] {} - {}ms - Response: {}",
                        apiInfo, executionTime, sanitizedResult);
            }
        } catch (Exception e) {
            log.warn("[LOG-ERROR] Failed to log success response for {}: {}", apiInfo, e.getMessage());
        }
    }
    
    private void logErrorResponse(String apiInfo, Exception e, long executionTime) {
        if (e instanceof CustomException) {
            CustomException customException = (CustomException) e;
            log.warn("[API-CUSTOM-ERROR] {} - {}ms - Code: {} - Message: {}",
                    apiInfo, executionTime, customException.getErrorCode(), customException.getMessage());
        } else {
            log.error("[API-SYSTEM-ERROR] {} - {}ms - Error: {} - Message: {}",
                    apiInfo, executionTime, e.getClass().getSimpleName(), e.getMessage());
        }
    }
    
    private String sanitizeAndTruncateResponse(Object response) {
        if (response == null) {
            return "null";
        }
        
        try {
            String jsonString;
            
            if (response instanceof Collection) {
                Collection<?> collection = (Collection<?>) response;
                jsonString = String.format("[Collection size: %d]", collection.size());
            } else if (response instanceof Page) {
                Page<?> page = (Page<?>) response;
                jsonString = String.format("[Page: %d/%d, size: %d]", 
                    page.getNumber() + 1, page.getTotalPages(), page.getSize());
            } else {
                jsonString = objectMapper.writeValueAsString(response);
            }
            
            String sanitized = maskSensitiveData(jsonString);
            
            if (sanitized.length() > MAX_RESPONSE_LENGTH) {
                return sanitized.substring(0, MAX_RESPONSE_LENGTH) + "... (truncated)";
            }
            
            return sanitized;
            
        } catch (Exception e) {
            return response.getClass().getSimpleName() + " (serialization failed)";
        }
    }
    
    private String maskSensitiveData(String json) {
        for (String field : SENSITIVE_FIELDS) {
            json = json.replaceAll(
                "(?i)(\"" + field + "\"\\s*:\\s*\")([^\"]*)(\")", 
                "$1***MASKED***$3"
            );
            json = json.replaceAll(
                "(?i)(" + field + "=)([^&\\s]*)", 
                "$1***MASKED***"
            );
        }
        return json;
    }
    
    private String getApiInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getMethod() + " " + request.getRequestURI();
            }
        } catch (Exception e) {
            // RequestContextHolder가 없는 경우 (비동기 처리 등)
        }
        return "UNKNOWN API";
    }

    private boolean isProductionEnvironment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
    
    private boolean isGetRequest(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return method.isAnnotationPresent(GetMapping.class);
    }
}