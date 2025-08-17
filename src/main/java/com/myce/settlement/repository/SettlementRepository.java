package com.myce.settlement.repository;

import com.myce.settlement.entity.Settlement;
import com.myce.settlement.entity.code.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    // Refactored to use 'Between' query
    Long countSettlementBySettlementAtBetweenAndSettlementStatus(LocalDateTime settlementAtAfter,
             LocalDateTime settlementAtBefore, SettlementStatus settlementStatus);

    // Refactored to use 'Between' query with a custom JPQL
    @Query("SELECT SUM(s.totalAmount - s.supplyAmount) " +
            "FROM Settlement s " +
            "WHERE s.settlementStatus = :status AND s.updatedAt BETWEEN :updatedAtAfter AND :updatedAtBefore")
    Long sumRevenueByStatusAndUpdatedAtBetween(
            @Param("status") SettlementStatus status,
            @Param("updatedAtAfter") LocalDateTime updatedAtAfter,
            @Param("updatedAtBefore") LocalDateTime updatedAtBefore
    );

    Optional<Settlement> findByExpoId(Long expoId);

    boolean existsByExpoId(Long expoId);
}