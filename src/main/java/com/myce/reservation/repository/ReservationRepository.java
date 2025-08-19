package com.myce.reservation.repository;

import com.myce.expo.entity.Expo;
import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(value = "SELECT r FROM Reservation r " +
            "WHERE r.userType = :userType AND r.userId = :userId",
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "WHERE r.userType = :userType AND r.userId = :userId")
    Page<Reservation> findReservationsByUserTypeAndUserIdWithExpoAndTicket(@Param("userType") UserType userType,
                                                                           @Param("userId") Long userId,
                                                                           Pageable pageable);

    @Query("""
            SELECT r, rpi, p, mg 
            FROM Reservation r 
            JOIN FETCH r.expo e 
            JOIN FETCH r.ticket t 
            LEFT JOIN ReservationPaymentInfo rpi ON rpi.reservation.id = r.id 
            LEFT JOIN Payment p ON p.targetType = 'RESERVATION' AND p.targetId = r.id 
            LEFT JOIN Member m ON r.userType = 'MEMBER' AND r.userId = m.id 
            LEFT JOIN m.memberGrade mg 
            WHERE r.userType = :userType AND r.userId = :userId 
            ORDER BY r.createdAt DESC
            """)
    Page<Object[]> findReservationsWithPaymentInfoByUserTypeAndUserId(@Param("userType") UserType userType,
                                                                      @Param("userId") Long userId,
                                                                      Pageable pageable);

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.expo e " +
            "JOIN FETCH r.ticket t " +
            "WHERE r.reservationCode = :reservationCode")
    Optional<Reservation> findByReservationCodeWithExpoAndTicket(@Param("reservationCode") String reservationCode);

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.expo e " +
            "JOIN FETCH r.ticket t " +
            "WHERE r.id = :reservationId")
    Optional<Reservation> findByIdWithExpoAndTicket(@Param("reservationId") Long reservationId);

    @Query("""
            SELECT new com.myce.reservation.dto.ExpoAdminPaymentBasicResponse(
                r.id,
                r.reservationCode,
                CASE
                    WHEN r.userType = com.myce.reservation.entity.code.UserType.MEMBER THEN m.name
                    ELSE g.name
                END,
                r.userType,
                CASE
                    WHEN r.userType = com.myce.reservation.entity.code.UserType.MEMBER THEN m.loginId
                    ELSE '-'
                END,
                CASE
                    WHEN r.userType = com.myce.reservation.entity.code.UserType.MEMBER THEN m.phone
                    ELSE g.phone
                END,
                CASE
                    WHEN r.userType = com.myce.reservation.entity.code.UserType.MEMBER THEN m.email
                    ELSE g.email
                END,
                r.quantity,
                (p.totalAmount + p.usedMileage - r.quantity * 1000),
                r.status,
                r.createdAt
            )
            FROM ReservationPaymentInfo p
            JOIN p.reservation r
            LEFT JOIN Member m ON r.userType = com.myce.reservation.entity.code.UserType.MEMBER AND r.userId = m.id
            LEFT JOIN Guest g ON r.userType = com.myce.reservation.entity.code.UserType.GUEST AND r.userId = g.id
            WHERE r.expo.id = :expoId
            AND (:status IS NULL OR r.status = :status)
            AND (
                (:name IS NULL OR (
                    (r.userType = com.myce.reservation.entity.code.UserType.MEMBER AND LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) OR
                    (r.userType = com.myce.reservation.entity.code.UserType.GUEST AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')))
                ))
            )
            AND (
                (:phone IS NULL OR (
                    (r.userType = com.myce.reservation.entity.code.UserType.MEMBER AND m.phone LIKE CONCAT('%', :phone, '%')) OR
                    (r.userType = com.myce.reservation.entity.code.UserType.GUEST AND g.phone LIKE CONCAT('%', :phone, '%'))
                ))
            )
            """)
    Page<ExpoAdminPaymentBasicResponse> findAllResponsesByExpoId(
            @Param("expoId") Long expoId,
            @Param("status") ReservationStatus status,
            @Param("name") String name,
            @Param("phone") String phone,
            Pageable pageable
    );

    @Query("""
            select distinct r.userId
            from Reservation r
            where r.expo.id = :expoId
              and r.status = :status
              and r.userType = :userType
            """)
    List<Long> findAllMemberIdByExpoIdAndStatusAndUserType(
            @Param("expoId") Long expoId,
            @Param("status") ReservationStatus status,
            @Param("userType") UserType userType
    );

    Optional<Reservation> findByReservationCode(String reservationCode);

    List<Reservation> findByExpoIn(List<Expo> expos);

    // reservation code 이미 있는지 확인
    boolean existsByReservationCode(String reservationCode);

    long countAllByCreatedAtAfter(LocalDateTime createdAt);

    List<Reservation> findByExpoId(Long expoId);

    @Query("SELECT DISTINCT r.userId FROM Reservation r " +
            "WHERE r.expo.id = :expoId AND r.userType = 'MEMBER'")
    List<Long> findDistinctUserIdsByExpoId(Long expoId);

    // === 대시보드 통계용 쿼리 메서드들 ===

    // 특정 박람회의 누적 판매 개수 (현재 존재하는 티켓의 확정된 예약 수량 총합)
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM Reservation r " +
           "JOIN r.ticket t " +
           "WHERE r.expo.id = :expoId AND r.status = 'CONFIRMED' AND t.expo.id = :expoId")
    Long countTotalReservationsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 오늘 판매 개수 (현재 존재하는 티켓의 확정된 예약 수량 총합, 00:00~23:59)
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM Reservation r " +
            "JOIN r.ticket t " +
            "WHERE r.expo.id = :expoId " +
            "AND r.status = 'CONFIRMED' " +
            "AND r.createdAt >= :startOfDay " +
            "AND r.createdAt < :startOfNextDay " +
            "AND t.expo.id = :expoId")
    Long countTodayReservationsByExpoId(@Param("expoId") Long expoId,
                                        @Param("startOfDay") LocalDateTime startOfDay,
                                        @Param("startOfNextDay") LocalDateTime startOfNextDay);

    // 특정 박람회의 날짜별 판매 수 (현재 존재하는 티켓의 확정된 예약만, 실제 수량 기준)
    @Query("SELECT DATE(r.createdAt) as date, COALESCE(SUM(r.quantity), 0) as count " +
            "FROM Reservation r " +
            "JOIN r.ticket t " +
            "WHERE r.expo.id = :expoId " +
            "AND r.status = 'CONFIRMED' " +
            "AND r.createdAt >= :startDate " +
            "AND r.createdAt <= :endDate " +
            "AND t.expo.id = :expoId " +
            "GROUP BY DATE(r.createdAt) " +
            "ORDER BY DATE(r.createdAt)")
    List<Object[]> countReservationsByDateRange(@Param("expoId") Long expoId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // 특정 박람회의 결제 대기 중인 예약 건수
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.expo.id = :expoId AND r.status = 'CONFIRMED_PENDING'")
    Long countPendingReservationsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 확정된 예약 건수 (결제 완료)
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.expo.id = :expoId AND r.status = 'CONFIRMED'")
    Long countConfirmedReservationsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 취소된 예약 건수 (환불)
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.expo.id = :expoId AND r.status = 'CANCELLED'")
    Long countCancelledReservationsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 티켓 종류별 판매 현황 (현재 존재하는 티켓의 확정된 예약만)
    @Query("SELECT t.name as ticketType, " +
            "t.totalQuantity as totalQuantity, " +
            "COALESCE(SUM(r.quantity), 0) as soldCount, " +
            "(t.totalQuantity - COALESCE(SUM(r.quantity), 0)) as remainingCount, " +
            "t.price as unitPrice, " +
            "COALESCE(SUM(r.quantity * t.price), 0) as totalRevenue " +
            "FROM Ticket t " +
            "LEFT JOIN Reservation r ON r.ticket.id = t.id AND r.status = 'CONFIRMED' AND r.expo.id = :expoId " +
            "WHERE t.expo.id = :expoId " +
            "GROUP BY t.id, t.name, t.totalQuantity, t.price " +
            "ORDER BY totalRevenue DESC")
    List<Object[]> getTicketSalesDetailByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 오늘 총 수익 (reservation 기준)
    @Query("SELECT COALESCE(SUM(r.quantity * t.price), 0) FROM Reservation r " +
            "JOIN r.ticket t " +
            "WHERE r.expo.id = :expoId " +
            "AND r.status = 'CONFIRMED' " +
            "AND DATE(r.createdAt) = :today")
    BigDecimal sumTodayRevenueByExpoId(@Param("expoId") Long expoId, @Param("today") LocalDate today);

    // 특정 박람회의 총 수익 (reservation 기준)
    @Query("SELECT COALESCE(SUM(r.quantity * t.price), 0) FROM Reservation r " +
            "JOIN r.ticket t " +
            "WHERE r.expo.id = :expoId " +
            "AND r.status = 'CONFIRMED'")
    BigDecimal sumTotalRevenueByExpoId(@Param("expoId") Long expoId);

    List<Reservation> findByUserIdAndUserTypeAndStatus(Long userId, UserType userType, ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = 'CONFIRMED' " +
            "AND r.createdAt BETWEEN :createdAtAfter AND :createdAtBefore")
    Long countAllByCreatedAtBetween(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);

    @Query("""
            SELECT r FROM Reservation r 
            JOIN FETCH r.expo e 
            JOIN FETCH r.ticket t 
            LEFT JOIN Guest g ON r.userType = com.myce.reservation.entity.code.UserType.GUEST AND r.userId = g.id 
            LEFT JOIN Member m ON r.userType = com.myce.reservation.entity.code.UserType.MEMBER AND r.userId = m.id 
            WHERE r.reservationCode = :reservationCode 
            AND (
                (r.userType = com.myce.reservation.entity.code.UserType.GUEST AND g.email = :email) OR 
                (r.userType = com.myce.reservation.entity.code.UserType.MEMBER AND m.email = :email)
            )
            """)
    Optional<Reservation> findByReservationCodeAndEmail(@Param("reservationCode") String reservationCode, 
                                                        @Param("email") String email);
    
    // 가상계좌 만료 처리용
    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime createdAt);
}
