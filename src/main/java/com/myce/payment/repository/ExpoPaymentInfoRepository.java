package com.myce.payment.repository;

import com.myce.payment.entity.ExpoPaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpoPaymentInfoRepository extends JpaRepository<ExpoPaymentInfo, Long> {
    
    Optional<ExpoPaymentInfo> findByExpoId(Long expoId);
}