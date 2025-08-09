package com.myce.common.repository;

import com.myce.common.entity.RejectInfo;
import com.myce.common.entity.type.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RejectInfoRepository extends JpaRepository <RejectInfo, Long> {
    Optional<RejectInfo> findByTargetIdAndTargetType(Long targetId, TargetType targetType);
}
