package com.myce.payment.repository;

import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.reservation.entity.Reservation;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReservationPaymentInfoRepository extends JpaRepository<ReservationPaymentInfo, Long> {
    Optional<ReservationPaymentInfo> findByReservationId(Long reservationId);

    // === 대시보드 통계용 쿼리 메서드들 ===

    // 특정 박람회의 결제 대기 건수
    @Query("SELECT COUNT(rpi) FROM ReservationPaymentInfo rpi " +
           "JOIN rpi.reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND rpi.status = 'PENDING'")
    Long countPendingPaymentsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 결제 완료 건수
    @Query("SELECT COUNT(rpi) FROM ReservationPaymentInfo rpi " +
           "JOIN rpi.reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND rpi.status = 'SUCCESS'")
    Long countCompletedPaymentsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 실패된 결제 건수
    @Query("SELECT COUNT(rpi) FROM ReservationPaymentInfo rpi " +
           "JOIN rpi.reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND rpi.status = 'FAILED'")
    Long countCancelledPaymentsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 환불된 결제 건수
    @Query("SELECT COUNT(rpi) FROM ReservationPaymentInfo rpi " +
           "JOIN rpi.reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND rpi.status = 'REFUNDED'")
    Long countRefundedPaymentsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 오늘 총 수익
    @Query("SELECT COALESCE(SUM(rpi.totalAmount), 0) FROM ReservationPaymentInfo rpi " +
           "JOIN rpi.reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND rpi.status = 'SUCCESS' " +
           "AND DATE(rpi.updatedAt) = :today")
    BigDecimal sumTodayRevenueByExpoId(@Param("expoId") Long expoId, @Param("today") LocalDate today);

    // 특정 박람회의 총 수익 (누적)
    @Query("SELECT COALESCE(SUM(rpi.totalAmount), 0) FROM ReservationPaymentInfo rpi " +
           "JOIN rpi.reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND rpi.status = 'SUCCESS'")
    BigDecimal sumTotalRevenueByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 티켓 종류별 판매 현황
    @Query("SELECT t.name as ticketType, " +
           "COUNT(rpi) as soldCount, " +
           "AVG(rpi.totalAmount) as avgPrice, " +
           "SUM(rpi.totalAmount) as totalRevenue " +
           "FROM ReservationPaymentInfo rpi " +
           "JOIN rpi.reservation r " +
           "JOIN r.ticket t " +
           "WHERE r.expo.id = :expoId " +
           "AND rpi.status = 'SUCCESS' " +
           "GROUP BY t.name " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> getTicketSalesDetailByExpoId(@Param("expoId") Long expoId);

    @Query("SELECT SUM(rpi.totalAmount) FROM ReservationPaymentInfo rpi WHERE rpi.reservation IN :reservations")
    Optional<Integer> findTotalAmountByReservations(@Param("reservations") List<Reservation> reservations);

}