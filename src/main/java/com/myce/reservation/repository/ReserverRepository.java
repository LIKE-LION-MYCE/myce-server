package com.myce.reservation.repository;

import com.myce.reservation.entity.Reserver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReserverRepository extends JpaRepository<Reserver, Long> {

}