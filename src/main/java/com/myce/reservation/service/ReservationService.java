package com.myce.reservation.service;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.reservation.dto.PreReservationRequest;
import com.myce.reservation.dto.PreReservationResponse;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPaymentSummaryResponse;
import com.myce.reservation.dto.ReservationPendingRequest;
import com.myce.reservation.dto.ReservationSuccessResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;

public interface ReservationService {
    
    ReservationDetailResponse getReservationDetail(Long reservationId, CustomUserDetails currentUser);
    
    void updateReservers(Long reservationId, ReserverBulkUpdateRequest request, CustomUserDetails currentUser);

    Long saveReservationPending(ReservationPendingRequest request);

    void updateStatusToConfirm(Long reservationId);

    ReservationSuccessResponse getReservationCodeAndEmail(Long reservationId);

    PreReservationResponse savePreReservation(PreReservationRequest request);

    ReservationPaymentSummaryResponse getPaymentSummary(Long reservationId);
}