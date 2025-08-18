package com.myce.reservation.repository;

import com.myce.reservation.dto.PreReservationCacheDto;

public interface PreReservationRepository {
    void save(PreReservationCacheDto cacheDto, int limitTime);

    PreReservationCacheDto findById(Long id);
    
    PreReservationCacheDto findByReservationCode(String reservationCode);
    
    void delete(Long id);
    
    void deleteByReservationCode(String reservationCode);
}