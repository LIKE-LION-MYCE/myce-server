package com.myce.payment.repository;

import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdPaymentInfoRepository extends JpaRepository<AdPaymentInfo, Long> {
    
    Optional<AdPaymentInfo> findByAdvertisementId(Long advertisementId);

    @Query("SELECT SUM(a.totalAmount) FROM AdPaymentInfo a " +
            "WHERE a.status IN :statuses AND a.updatedAt > :timestamp")
    Long sumTotalAmountByStatusAndUpdatedAtAfter(List<PaymentStatus> statuses, LocalDateTime timestamp);
}