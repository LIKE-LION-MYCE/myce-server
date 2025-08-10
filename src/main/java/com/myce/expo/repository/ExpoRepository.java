package com.myce.expo.repository;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
    Boolean existsByIdAndMemberId(Long id, Long memberId);
}

