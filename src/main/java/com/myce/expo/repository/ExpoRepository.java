package com.myce.expo.repository;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {
    Optional<Expo> findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(Long memberId, List<ExpoStatus> status);
    List<Expo> findByMemberIdAndStatusIn(Long memberId, List<ExpoStatus> status);
    Boolean existsByIdAndMemberId(Long id, Long memberId);
}

