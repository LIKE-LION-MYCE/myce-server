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

    List<Expo> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    Page<Expo> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Boolean existsByIdAndMemberId(Long id, Long memberId);
    
    // QR코드 일괄 생성용 - 시작일이 특정 날짜이고 게시된 박람회 조회
    List<Expo> findByStartDateAndStatus(LocalDate startDate, ExpoStatus status);
}

