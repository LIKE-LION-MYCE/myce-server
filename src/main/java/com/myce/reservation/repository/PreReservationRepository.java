package com.myce.reservation.repository;

import com.myce.reservation.dto.PreReservationCacheDto;

public interface PreReservationRepository {
    String saveWithUniqueKey(PreReservationCacheDto cacheDto, int limitTime);

    PreReservationCacheDto findById(Long id);

    PreReservationCacheDto findBySessionId(String sessionId);

    void delete(Long id);

    void deleteBySessionId(String sessionId);
}