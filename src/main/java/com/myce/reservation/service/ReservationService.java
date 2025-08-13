package com.myce.reservation.service;

import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPendingRequest;
import com.myce.reservation.dto.ReservationSuccessResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;

public interface ReservationService {
    
    ReservationDetailResponse getReservationDetail(String reservationCode);
    
    void updateReservers(String reservationCode, ReserverBulkUpdateRequest request);

    Long saveReservationPending(ReservationPendingRequest request);

    void updateStatusToConfirm(Long reservationId);

    ReservationSuccessResponse getReservationCodeAndEmail(Long reservationId);
}