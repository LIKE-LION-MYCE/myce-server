package com.myce.expo.repository;

import com.myce.expo.entity.Booth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoothRepository extends JpaRepository<Booth, Long> {
    long countByExpoIdAndIsPremiumTrue(Long expoId);

    boolean existsByExpoIdAndIsPremiumTrueAndDisplayRank(Long expoId, Integer displayRank);

    List<Booth> findAllByExpoId(Long expoId);

    // 프리미엄 부스가 displayRank 순서에 따라 먼저 표시
    @Query("SELECT b FROM Booth b WHERE b.expo.id = :expoId ORDER BY b.isPremium DESC, b.displayRank ASC")
    List<Booth> findByExpoIdSorted(Long expoId);
}
