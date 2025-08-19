package com.myce.payment.repository;

import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.payment.entity.AdPaymentInfo;
import io.lettuce.core.dynamic.annotation.Param;
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
            "WHERE a.advertisement.status IN :statuses AND a.updatedAt BETWEEN :updatedAtAfter AND :updatedAtBefore")
    Long sumTotalAmountByStatusAndUpdatedAtBetween(
            @Param("statuses") List<AdvertisementStatus> statuses,
            @Param("updatedAtAfter") LocalDateTime updatedAtAfter,
            @Param("updatedAtBefore") LocalDateTime updatedAtBefore
    );
}