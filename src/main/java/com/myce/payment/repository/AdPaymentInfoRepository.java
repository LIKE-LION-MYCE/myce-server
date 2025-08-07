package com.myce.payment.repository;

import com.myce.payment.entity.AdPaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdPaymentInfoRepository extends JpaRepository<AdPaymentInfo, Long> {
    
    Optional<AdPaymentInfo> findByAdvertisementId(Long advertisementId);
}