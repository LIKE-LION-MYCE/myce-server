package com.myce.payment.repository;

import com.myce.expo.entity.type.ExpoStatus;
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

    @Query("SELECT SUM(a.totalAmount) FROM ExpoPaymentInfo a " +
            "WHERE a.expo.status IN :statuses AND a.updatedAt BETWEEN :updatedAtAfter AND :updatedAtBefore")
    Long sumTotalAmountByStatusesAndUpdatedAtBetween(
            @Param("statuses") List<ExpoStatus> statuses,
            @Param("updatedAtAfter") LocalDateTime updatedAtAfter,
            @Param("updatedAtBefore") LocalDateTime updatedAtBefore
    );
}