package com.myce.reservation.service;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;

public interface ReservationService {
    
    ReservationDetailResponse getReservationDetail(Long reservationId, CustomUserDetails currentUser);
    
    void updateReservers(Long reservationId, ReserverBulkUpdateRequest request, CustomUserDetails currentUser);
}