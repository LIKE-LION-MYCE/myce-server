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
/**
 * 검증된 베스트 프랙티스 기반 설정
 * 
 * 이 방법은 Stack Overflow, Spring 공식 문서, GitHub 이슈에서 
 * 다중 데이터 스토어 환경에서 권장하는 표준 방법입니다.
 * 
 * 참고:
 * - Spring Data JPA Issue #2740
 * - Stack Overflow: Multiple Spring Data modules configuration
 * - Baeldung: Configure Multiple DataSources
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.myce",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = JpaRepository.class
    )
)
@EnableMongoRepositories(
    basePackages = "com.myce",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = MongoRepository.class
    )
)
// Redis Repository 설정 (현재 비활성화)
// 팀에서 현재 RedisTemplate만 사용 중, Repository 패턴은 미사용
// Redis Repository 도입 시 아래 주석 해제 + application.yml에서 enabled: true 설정
/*
@EnableRedisRepositories(
    basePackages = "com.myce",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = KeyValueRepository.class
    )
)
*/
public class MultiDataSourceConfig {
    
    /**
     * 이 설정이 작동하는 이유:
     * 
     * 1. @Configuration 클래스의 직접 어노테이션이 Spring Boot 자동설정보다 우선함
     * 2. includeFilters로 각 모듈이 자신의 Repository 타입만 스캔
     * 3. FilterType.ASSIGNABLE_TYPE이 상속 관계를 정확히 감지
     * 4. basePackages로 전체 패키지를 스캔하되 타입으로 필터링
     * 
     * 결과: "Could not safely identify store assignment" 경고 완전 제거
     */
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

3. Redis Entity & Repository (미래 사용시):
   @RedisHash("email_verification")
   public class EmailVerification { ... }
   
   public interface EmailVerificationRepository extends KeyValueRepository<EmailVerification, String> { ... }

Redis Repository 활성화 방법:
1. 위 @EnableRedisRepositories 주석 해제
2. application.yml에서 spring.data.redis.repositories.enabled: true 설정

장점:
✅ 새 Repository 추가시 자동 감지 (설정 파일 수정 불필요)
✅ 타입 안전성 보장 (잘못된 매핑 방지)
✅ Spring Data 경고 메시지 완전 제거
✅ 확장 가능한 구조 (새로운 데이터 스토어 쉽게 추가)
✅ 팀원들이 실수할 여지 최소화
*/