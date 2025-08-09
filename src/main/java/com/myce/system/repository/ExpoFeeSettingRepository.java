package com.myce.system.repository;

import com.myce.system.entity.ExpoFeeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpoFeeSettingRepository extends JpaRepository<ExpoFeeSetting, Long> {
    Optional<ExpoFeeSetting> findByIsActiveTrue();
}
