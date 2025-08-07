package com.myce.reservation.repository;

import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
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
}