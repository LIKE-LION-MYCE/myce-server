package com.myce.reservation.service;

import com.myce.reservation.dto.ReserverUpdateRequest;

public interface ReserverService {
    
    void updateReserver(Long reserverId, ReserverUpdateRequest requestDto);
}