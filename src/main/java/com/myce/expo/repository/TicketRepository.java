package com.myce.expo.repository;

import com.myce.expo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByExpoId(Long id);
        List<Ticket> findByExpoIdOrderByTypeAscSaleStartDateAsc(Long expoId);
    List<Ticket> findByExpoIdOrderByCreatedAtAsc(Long expoId);

    @Query("""
        select t
        from Ticket t
        where t.expo.id = :expoId
          and :today between t.useStartDate and t.useEndDate
    """)
    List<Ticket> findAllByExpoIdAndDateContains(@Param("expoId") Long expoId,
                                                @Param("today") LocalDate today);
}

