package com.myce.system.repository;

import com.myce.system.entity.AdFeeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdFeeSettingRepository extends JpaRepository<AdFeeSetting, Long> {
    Optional<AdFeeSetting> findByAdPositionIdAndIsActiveTrue(Long adPositionId);
}
