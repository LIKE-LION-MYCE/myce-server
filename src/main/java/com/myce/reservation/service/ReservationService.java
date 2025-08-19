package com.myce.reservation.service;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.reservation.dto.PreReservationRequest;
import com.myce.reservation.dto.PreReservationResponse;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPaymentSummaryResponse;
import com.myce.reservation.dto.ReservationPendingResponse;
import com.myce.reservation.dto.ReservationSuccessResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;

public interface ReservationService {
    
    ReservationDetailResponse getReservationDetail(Long reservationId, CustomUserDetails currentUser);
    
    void updateReservers(Long reservationId, ReserverBulkUpdateRequest request, CustomUserDetails currentUser);

    void updateStatusToConfirm(Long reservationId);

    ReservationSuccessResponse getReservationCodeAndEmail(Long reservationId);

    PreReservationResponse savePreReservation(PreReservationRequest request);

    ReservationPaymentSummaryResponse getPaymentSummary(Long reservationId);
    
    ReservationPaymentSummaryResponse getPaymentSummaryBySessionId(String sessionId);

    void deletePendingReservation(Long reservationId);

    ReservationPendingResponse getVirtualAccountInfo(Long reservationId);

    ReservationDetailResponse getNonMemberReservationDetail(String email, String reservationCode);
}