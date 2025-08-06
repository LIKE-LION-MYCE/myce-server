package com.myce.common.repository;

import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    Optional<BusinessProfile> findByTargetIdAndTargetType(Long targetId, TargetType targetType);
}