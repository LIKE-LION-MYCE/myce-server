package com.myce.expo.repository;

import com.myce.expo.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByExpoId(Long expoId);

    List<Event> findAllByExpoIdOrderByEventDateAscStartTimeAsc(Long expoId);

    List<Event> findByEventDateAndStartTimeBetween(LocalDate eventDate, LocalTime startTime, LocalTime endTime);
}
