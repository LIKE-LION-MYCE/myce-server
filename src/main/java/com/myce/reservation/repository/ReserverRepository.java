package com.myce.reservation.repository;

import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReserverRepository extends JpaRepository<Reserver, Long> {
    
    List<Reserver> findByReservation(Reservation reservation);
    
    // QR코드 일괄 생성용 - 특정 박람회의 모든 예약자 조회
    @Query("SELECT r FROM Reserver r " +
           "JOIN r.reservation res " +
           "WHERE res.expo.id = :expoId ")
    List<Reserver> findReserversByExpo(@Param("expoId") Long expoId);
}