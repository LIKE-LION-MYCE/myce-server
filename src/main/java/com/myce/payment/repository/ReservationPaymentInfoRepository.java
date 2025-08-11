package com.myce.payment.repository;

import com.myce.payment.entity.ReservationPaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationPaymentInfoRepository extends JpaRepository<ReservationPaymentInfo, Long> {
    Optional<ReservationPaymentInfo> findByReservationId(Long reservationId);
}