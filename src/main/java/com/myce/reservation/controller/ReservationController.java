package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.reservation.dto.PreReservationRequest;
import com.myce.reservation.dto.PreReservationResponse;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPaymentSummaryResponse;
import com.myce.reservation.dto.ReservationSuccessResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;
import com.myce.reservation.dto.GuestReservationRequest;
import com.myce.reservation.service.ReservationService;
import com.myce.reservation.service.GuestReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    private final GuestReservationService guestReservationService;

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> getReservationDetail(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        ReservationDetailResponse reservationDetail = reservationService.getReservationDetail(reservationId, currentUser);
        
        return ResponseEntity.ok(reservationDetail);
    }
    
    @PutMapping("/{reservationId}/reservers")
    public ResponseEntity<Void> updateReservers(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReserverBulkUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        reservationService.updateReservers(reservationId, request, currentUser);
        
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/guestId")
    public ResponseEntity<Void> updateGuestId(
        @Valid @RequestBody GuestReservationRequest request
    ) {
        guestReservationService.updateGuestId(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{reservationId}/confirm")
    public ResponseEntity<Void> updateReservationStatusConfirm(@PathVariable Long reservationId){
        reservationService.updateStatusToConfirm(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reservationId}/success")
    public ResponseEntity<ReservationSuccessResponse> getReservationSuccess(
        @PathVariable Long reservationId){
        ReservationSuccessResponse response = reservationService.getReservationCodeAndEmail(reservationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pre-reservation")
    public ResponseEntity<PreReservationResponse> savePreReservation(
        @Valid @RequestBody PreReservationRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.savePreReservation(request));
    }

    @GetMapping("/{reservationId}/payment-summary")
    public ResponseEntity<ReservationPaymentSummaryResponse> getPaymentSummary(@PathVariable Long reservationId){
        ReservationPaymentSummaryResponse response = reservationService.getPaymentSummary(reservationId);
        return ResponseEntity.ok(response);
    }
}