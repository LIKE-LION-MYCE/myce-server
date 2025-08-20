package com.myce.system.repository;

import com.myce.system.entity.RefundFeeSetting;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundFeeSettingRepository extends JpaRepository<RefundFeeSetting, Long> {

    Page<RefundFeeSetting> findAll(Pageable pageable);

    @Query("SELECT r FROM RefundFeeSetting r WHERE r.isActive = true AND r.validFrom <= :now AND r.validUntil >= :now ORDER BY r.standardDayCount DESC")
    List<RefundFeeSetting> findActiveRefundSettings(@Param("now") LocalDateTime now);

    Page<RefundFeeSetting> findAllByNameContains(String name, Pageable pageable);

    boolean existsByValidFromBeforeAndValidUntilAfter(LocalDateTime validFrom, LocalDateTime validUntil);

}