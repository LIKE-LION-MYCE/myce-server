package com.myce.payment.repository;

import com.myce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    @Query("SELECT p, rpi, rpi.reservation, rpi.reservation.expo FROM Payment p " +
           "JOIN ReservationPaymentInfo rpi ON p.targetId = rpi.id " +
           "WHERE rpi.reservation.member.id = :memberId " +
           "AND p.targetType = 'RESERVATION' " +
           "ORDER BY p.createdAt DESC")
    List<Object[]> findReservationPaymentHistoryByMemberId(@Param("memberId") Long memberId);
}