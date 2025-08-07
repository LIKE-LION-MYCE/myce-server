package com.myce.reservation.service;

import com.myce.reservation.dto.ReservationDetailResponse;

public interface ReservationService {
    
    ReservationDetailResponse getReservationDetail(String reservationCode);
}