package com.myce.expo.repository;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {
    Optional<Expo> findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(Long memberId, List<ExpoStatus> status);

    @Query("""
        select e.id
        from Expo e
        where e.member.id = :memberId
          and e.status in :statuses
    """)
    List<Long> findIdsByMemberIdAndStatusIn(@Param("memberId") Long memberId,
                                            @Param("statuses") Collection<ExpoStatus> statuses);

    // 스케줄러용 - 활성 상태인 모든 박람회 ID 조회
    @Query("""
        select e.id
        from Expo e
        where e.status in :statuses
    """)
    List<Long> findIdsByStatusIn(@Param("statuses") Collection<ExpoStatus> statuses);
    
    // 스케줄러용 - 특정 상태의 박람회 ID 조회
    @Query("""
        select e.id
        from Expo e
        where e.status = :status
    """)
    List<Long> findIdsByStatus(@Param("status") ExpoStatus status);

    List<Expo> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    Page<Expo> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Boolean existsByIdAndMemberId(Long id, Long memberId);
    
    // QR코드 일괄 생성용 - 시작일이 특정 날짜이고 게시된 박람회 조회
    List<Expo> findByStartDateAndStatus(LocalDate startDate, ExpoStatus status);
    
    // 플랫폼 관리자용 - 상태별 박람회 조회
    Page<Expo> findByStatusOrderByCreatedAtDesc(ExpoStatus status, Pageable pageable);
    Page<Expo> findByStatusOrderByCreatedAtAsc(ExpoStatus status, Pageable pageable);
    
    // 플랫폼 관리자용 - 전체 박람회 조회 (정렬 옵션)
    Page<Expo> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Expo> findAllByOrderByCreatedAtAsc(Pageable pageable);
    
    // 플랫폼 관리자용 - 키워드 검색 + 상태 필터링
    Page<Expo> findByTitleContainingAndStatusOrderByCreatedAtDesc(String keyword, ExpoStatus status, Pageable pageable);
    Page<Expo> findByTitleContainingAndStatusOrderByCreatedAtAsc(String keyword, ExpoStatus status, Pageable pageable);
    
    // 플랫폼 관리자용 - 키워드 검색 (상태 무관)
    Page<Expo> findByTitleContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);
    Page<Expo> findByTitleContainingOrderByCreatedAtAsc(String keyword, Pageable pageable);
    
    // 현재 박람회 관리용 - 상태별 조회 (정렬 옵션 포함)
    Page<Expo> findByStatus(ExpoStatus status, Pageable pageable);
    
    // 현재 박람회 관리용 - 키워드 검색 + 상태 필터링 (대소문자 구분 없음)
    Page<Expo> findByStatusAndTitleContainingIgnoreCase(ExpoStatus status, String keyword, Pageable pageable);
    
    // 현재 박람회 관리용 - 키워드 검색 (대소문자 구분 없음)
    Page<Expo> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    
    // 스케줄러용 - 게시 대기 중인 박람회 중 게시 시작일이 오늘 이전인 것들 조회
    List<Expo> findAllByDisplayStartDateLessThanEqualAndStatus(LocalDate date, ExpoStatus status);
    
    // 스케줄러용 - 게시 중인 박람회 중 게시 종료일이 오늘 이전인 것들 조회  
    List<Expo> findAllByDisplayEndDateLessThanAndStatus(LocalDate date, ExpoStatus status);
    
    // 현재 박람회 관리용 - 여러 상태 조회
    Page<Expo> findByStatusIn(List<ExpoStatus> statuses, Pageable pageable);
    
    // 현재 박람회 관리용 - 키워드 검색 + 여러 상태 조회
    Page<Expo> findByTitleContainingIgnoreCaseAndStatusIn(String keyword, List<ExpoStatus> statuses, Pageable pageable);

    @Query("select count(e) from Expo e " +
            "WHERE e.displayEndDate <= CURRENT_DATE " +
            "and e.displayEndDate >= :date ")
    long countAllAfterDate(LocalDate date);
    // 카테고리 필터링
    @Query("SELECT e FROM Expo e JOIN e.expoCategories ec WHERE ec.category.id = :categoryId")
    Page<Expo> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // PUBLISHED + 카테고리/기간/검색
    @Query("SELECT DISTINCT e FROM Expo e " +
           "JOIN e.expoCategories ec " +
           "JOIN ec.category c " +
           "WHERE e.status = :status " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:startDate IS NULL OR e.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR e.endDate <= :endDate)")
    Page<Expo> findPublishedExposFiltered(
        @Param("status") ExpoStatus status,
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
}

