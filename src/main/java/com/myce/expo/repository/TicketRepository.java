package com.myce.expo.repository;

import com.myce.expo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByExpoId(Long id);
        List<Ticket> findByExpoIdOrderByTypeAscSaleStartDateAsc(Long expoId);
    List<Ticket> findByExpoIdOrderByCreatedAtAsc(Long expoId);
}
