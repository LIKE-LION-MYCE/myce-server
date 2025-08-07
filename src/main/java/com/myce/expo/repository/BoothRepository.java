package com.myce.expo.repository;

import com.myce.expo.entity.Booth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoothRepository extends JpaRepository<Booth, Long> {
    long countByExpoIdAndIsPremiumTrue(Long expoId);

    boolean existsByExpoIdAndIsPremiumTrueAndDisplayRank(Long expoId, Integer displayRank);

    List<Booth> findAllByExpoId(Long expoId);
}
