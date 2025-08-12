package com.myce.system.repository;

import com.myce.system.entity.ExpoFeeSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpoFeeSettingRepository extends JpaRepository<ExpoFeeSetting, Long> {
    Optional<ExpoFeeSetting> findByIsActiveTrue();

    Page<ExpoFeeSetting> findAll(Pageable pageable);

    Page<ExpoFeeSetting> findAllByNameContaining(String name, Pageable pageable);
}
