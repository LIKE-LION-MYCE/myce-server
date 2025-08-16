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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.expo e " +
            "JOIN FETCH r.ticket t " +
            "WHERE r.userType = :userType AND r.userId = :userId " +
            "ORDER BY r.createdAt DESC")
    List<Reservation> findReservationsByUserTypeAndUserIdWithExpoAndTicket(@Param("userType") UserType userType,
                                                                           @Param("userId") Long userId);

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
                (p.totalAmount + p.usedMileage),
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

    // === 대시보드 통계용 쿼리 메서드들 ===

    // 특정 박람회의 누적 예약자 수
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.expo.id = :expoId AND r.status != 'CANCELLED'")
    Long countTotalReservationsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 오늘 예약자 수
    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND r.status != 'CANCELLED' " +
           "AND DATE(r.createdAt) = :today")
    Long countTodayReservationsByExpoId(@Param("expoId") Long expoId, @Param("today") LocalDate today);

    // 특정 박람회의 날짜별 예약자 수 (일주일)
    @Query("SELECT DATE(r.createdAt) as date, COUNT(r) as count " +
           "FROM Reservation r " +
           "WHERE r.expo.id = :expoId " +
           "AND r.status != 'CANCELLED' " +
           "AND r.createdAt >= :startDate " +
           "AND r.createdAt <= :endDate " +
           "GROUP BY DATE(r.createdAt) " +
           "ORDER BY DATE(r.createdAt)")
    List<Object[]> countReservationsByDateRange(@Param("expoId") Long expoId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    List<Reservation> findByUserIdAndUserTypeAndStatus(Long userId, UserType userType, ReservationStatus status);
}
