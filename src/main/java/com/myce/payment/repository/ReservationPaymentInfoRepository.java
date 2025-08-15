package com.myce.payment.repository;

import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.reservation.entity.Reservation;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationPaymentInfoRepository extends JpaRepository<ReservationPaymentInfo, Long> {
    Optional<ReservationPaymentInfo> findByReservationId(Long reservationId);

    @Query("SELECT SUM(rpi.totalAmount) FROM ReservationPaymentInfo rpi WHERE rpi.reservation IN :reservations")
    Optional<Integer> findTotalAmountByReservations(@Param("reservations") List<Reservation> reservations);
}