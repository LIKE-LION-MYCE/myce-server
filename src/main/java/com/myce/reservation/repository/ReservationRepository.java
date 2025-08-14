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

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.expo e " +
            "JOIN FETCH r.ticket t " +
            "WHERE r.userType = :userType AND r.userId = :userId")
    List<Reservation> findReservationsByUserTypeAndUserIdWithExpoAndTicket(@Param("userType") UserType userType,
                                                                           @Param("userId") Long userId);

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

    List<Reservation> findByExpoId(Long expoId);
}