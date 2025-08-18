package com.myce.reservation.repository;

import com.myce.reservation.dto.PreReservationCacheDto;

public interface PreReservationRepository {
    void save(PreReservationCacheDto cacheDto, int limitTime);

    PreReservationCacheDto findById(Long id);
    
    void delete(Long id);
}