package com.myce.reservation.service;

import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;

public interface ReservationService {
    
    ReservationDetailResponse getReservationDetail(String reservationCode);
    
    void updateReservers(String reservationCode, ReserverBulkUpdateRequest request);
}