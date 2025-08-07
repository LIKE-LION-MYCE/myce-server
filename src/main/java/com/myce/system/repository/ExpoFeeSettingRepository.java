package com.myce.system.repository;

import com.myce.system.entity.ExpoFeeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpoFeeSettingRepository extends JpaRepository<ExpoFeeSetting, Long> {
    
    @Query("SELECT efs FROM ExpoFeeSetting efs WHERE efs.isActive = true ORDER BY efs.createdAt DESC LIMIT 1")
    Optional<ExpoFeeSetting> findActiveFeeSetting();
}