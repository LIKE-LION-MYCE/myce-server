package com.myce.settlement.repository;

import com.myce.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByExpoId(Long expoId);

    boolean existsByExpoId(Long expoId);
}