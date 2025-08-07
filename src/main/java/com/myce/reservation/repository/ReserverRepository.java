package com.myce.reservation.repository;

import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReserverRepository extends JpaRepository<Reserver, Long> {
    
    List<Reserver> findByReservation(Reservation reservation);
}