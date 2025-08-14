package com.myce.settlement.repository;

import com.myce.settlement.entity.Settlement;
import com.myce.settlement.entity.code.SettlementStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Long countSettlementBySettlementAtAfterAndSettlementStatus(LocalDateTime settlementAtAfter, SettlementStatus settlementStatus);

    @Query("SELECT SUM(s.totalAmount - s.supplyAmount) " +
            "FROM Settlement s " +
            "WHERE s.settlementStatus = :status AND s.updatedAt >= :timestamp")
    Long sumRevenueByStatusAndUpdatedAtAfter(
            @Param("status") SettlementStatus status,
            @Param("timestamp") LocalDateTime timestamp
    );

    Optional<Settlement> findByExpoId(Long expoId);

    boolean existsByExpoId(Long expoId);
}