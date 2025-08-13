package com.myce.system.repository;

import com.myce.system.entity.AdFeeSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdFeeSettingRepository extends JpaRepository<AdFeeSetting, Long> {
    @EntityGraph(attributePaths = {"adPosition"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<AdFeeSetting> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"adPosition"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<AdFeeSetting> findAllByAdPosition_Id(Long positionId, Pageable pageable);

    @EntityGraph(attributePaths = {"adPosition"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<AdFeeSetting> findAllByNameContains(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"adPosition"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<AdFeeSetting> findAllByAdPosition_IdAndNameContaining(Long positionId, String name, Pageable pageable);

    Optional<AdFeeSetting> findByAdPositionIdAndIsActiveTrue(Long adPositionId);

    Optional<AdFeeSetting> findByAdPositionId(Long adPositionId);
}
