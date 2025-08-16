package com.myce.system.repository;

import com.myce.system.entity.RefundFeeSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundFeeSettingRepository extends JpaRepository<RefundFeeSetting, Long> {

    Page<RefundFeeSetting> findAll(Pageable pageable);
}