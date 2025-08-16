package com.myce.payment.repository;

import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpoPaymentInfoRepository extends JpaRepository<ExpoPaymentInfo, Long> {
    
    Optional<ExpoPaymentInfo> findByExpoId(Long expoId);

    @Query("SELECT SUM(e.totalAmount) FROM ExpoPaymentInfo e " +
            "WHERE e.status IN :statuses AND e.updatedAt > :timestamp")
    Long sumTotalAmountByStatusesAndUpdatedAtAfter(
            @Param("statuses") List<PaymentStatus> statuses,
            @Param("timestamp") LocalDateTime timestamp
    );
}