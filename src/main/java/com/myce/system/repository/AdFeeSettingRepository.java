package com.myce.system.repository;

import com.myce.system.entity.AdFeeSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdFeeSettingRepository extends JpaRepository<AdFeeSetting, Long> {
    Page<AdFeeSetting> findAll(Pageable pageable);
    Page<AdFeeSetting> findAllByAdPosition_Id(Long positionId, Pageable pageable);
    Optional<AdFeeSetting> findByAdPositionIdAndIsActiveTrue(Long adPositionId);
    Optional<AdFeeSetting> findByAdPositionId(Long adPositionId);
}
