package com.myce.payment.repository;

import com.myce.payment.entity.ReservationPaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationPaymentInfoRepository extends JpaRepository<ReservationPaymentInfo, Long> {
    
    @Query("SELECT rpi FROM ReservationPaymentInfo rpi " +
           "WHERE rpi.id = :paymentInfoId")
    Optional<ReservationPaymentInfo> findByIdWithMember(@Param("paymentInfoId") Long paymentInfoId);
}