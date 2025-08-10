package com.myce.payment.repository;

import com.myce.payment.entity.Payment;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.reservation.entity.code.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByImpUid(String impUid);
    Optional<Payment> findByMerchantUid(String merchantUid);
    
    @Query("SELECT p, rpi, rpi.reservation, rpi.reservation.expo FROM Payment p " +
           "JOIN ReservationPaymentInfo rpi ON p.targetId = rpi.id " +
           "WHERE rpi.reservation.userType = :userType AND rpi.reservation.userId = :userId " +
           "AND p.targetType = 'RESERVATION' " +
           "ORDER BY p.createdAt DESC")
    List<Object[]> findReservationPaymentHistoryByUserTypeAndUserId(@Param("userType") UserType userType,
                                                                    @Param("userId") Long userId);
    
    @Query("SELECT p, rpi, rpi.reservation, rpi.reservation.expo FROM Payment p " +
           "JOIN ReservationPaymentInfo rpi ON p.targetId = rpi.id " +
           "WHERE rpi.reservation.userType = :userType AND rpi.reservation.userId = :userId " +
           "AND p.targetType = 'RESERVATION' " +
           "ORDER BY p.createdAt DESC")
    Page<Object[]> findReservationPaymentHistoryByUserTypeAndUserId(@Param("userType") UserType userType,
                                                                    @Param("userId") Long userId,
                                                                    Pageable pageable);

    Optional<Payment> findByTargetIdAndTargetType(Long targetId, PaymentTargetType targetType);
}