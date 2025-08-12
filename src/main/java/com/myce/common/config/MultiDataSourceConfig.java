package com.myce.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * 다중 데이터 스토어 설정 클래스
 * 
 * Spring Boot가 여러 데이터 스토어(JPA, MongoDB, Redis)를 동시에 사용할 때
 * 각 Repository를 올바른 데이터 스토어에 매핑하기 위한 설정
 * 
 * 이 설정은 다음과 같은 문제를 해결합니다:
 * - "Could not safely identify store assignment" 경고 제거
 * - 각 Spring Data 모듈이 자신의 Repository만 관리하도록 제한
 * - 새로운 Repository 추가시 자동 감지 (유지보수 불필요)
 * 
 * 사용법:
 * 1. JPA Entity: @Entity 어노테이션 + JpaRepository 상속
 * 2. MongoDB Document: @Document 어노테이션 + MongoRepository 상속  
 * 3. Redis Entity: @RedisHash 어노테이션 + KeyValueRepository 상속
 * 
 * @author MYCE Team
 * @since 2025-08-11
 */
@Configuration
public class MultiDataSourceConfig {

    /**
     * JPA Repository 스캔 설정
     * 
     * JpaRepository를 상속한 인터페이스만 스캔하도록 제한
     * MySQL/MariaDB 등 관계형 데이터베이스용 Repository 자동 감지
     */
    @EnableJpaRepositories(
        basePackages = "com.myce",
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE, 
            classes = JpaRepository.class
        )
    )
    static class JpaRepositoryConfig {
        // JPA 관련 추가 설정이 필요하면 여기에 Bean 정의
    }

    /**
     * MongoDB Repository 스캔 설정
     * 
     * MongoRepository를 상속한 인터페이스만 스캔하도록 제한
     * NoSQL 문서 데이터베이스용 Repository 자동 감지
     */
    @EnableMongoRepositories(
        basePackages = "com.myce",
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE, 
            classes = MongoRepository.class
        )
    )
    static class MongoRepositoryConfig {
        // MongoDB 관련 추가 설정이 필요하면 여기에 Bean 정의
    }

    /**
     * Redis Repository 스캔 설정
     * 
     * KeyValueRepository를 상속한 인터페이스만 스캔하도록 제한
     * 캐시, 세션 등 Key-Value 저장소용 Repository 자동 감지
     */
    @EnableRedisRepositories(
        basePackages = "com.myce",
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE, 
            classes = KeyValueRepository.class
        )
    )
    static class RedisRepositoryConfig {
        // Redis 관련 추가 설정이 필요하면 여기에 Bean 정의
    }
}

/*
사용 예시:

1. MySQL Entity & Repository:
   @Entity
   public class Member { ... }
   
   public interface MemberRepository extends JpaRepository<Member, Long> { ... }

2. MongoDB Document & Repository:
   @Document(collection = "chat_messages")
   public class ChatMessage { ... }
   
   public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> { ... }

3. Redis Entity & Repository:
   @RedisHash("email_verification")
   public class EmailVerification { ... }
   
   public interface EmailVerificationRepository extends KeyValueRepository<EmailVerification, String> { ... }

장점:
✅ 새 Repository 추가시 자동 감지 (설정 파일 수정 불필요)
✅ 타입 안전성 보장 (잘못된 매핑 방지)
✅ Spring Data 경고 메시지 완전 제거
✅ 확장 가능한 구조 (새로운 데이터 스토어 쉽게 추가)
✅ 팀원들이 실수할 여지 최소화
*/