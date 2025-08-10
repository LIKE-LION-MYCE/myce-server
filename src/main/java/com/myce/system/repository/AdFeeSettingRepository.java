package com.myce.system.repository;

import com.myce.system.entity.AdFeeSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdFeeSettingRepository extends JpaRepository<AdFeeSetting, Long> {
    Optional<AdFeeSetting> findByAdPositionIdAndIsActiveTrue(Long adPositionId);
    Optional<AdFeeSetting> findByAdPositionId(Long adPositionId);
}
