package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.reservation.dto.PreReservationRequest;
import com.myce.reservation.dto.PreReservationResponse;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPaymentSummaryResponse;
import com.myce.reservation.dto.ReservationPendingResponse;
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

    @GetMapping("/{reservationId}/pending")
    public ResponseEntity<ReservationPendingResponse> getReservationPending(
        @PathVariable Long reservationId){
        ReservationPendingResponse response = reservationService.getVirtualAccountInfo(reservationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pre-reservation")
    public ResponseEntity<PreReservationResponse> savePreReservation(
        @Valid @RequestBody PreReservationRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.savePreReservation(request));
    }

    @GetMapping("/payment-summary")
    public ResponseEntity<ReservationPaymentSummaryResponse> getPaymentSummary(
            @RequestParam Long preReservationId,
            @RequestParam(required = false) String sessionId){
        
        ReservationPaymentSummaryResponse response;
        
        // 세션 ID가 제공되면 세션 기반 조회, 아니면 기존 방식
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            response = reservationService.getPaymentSummaryBySessionId(sessionId);
        } else {
            response = reservationService.getPaymentSummary(preReservationId);
        }
        
        return ResponseEntity.ok(response);
    }

    // 결제 실패 또는 취소 시, 삭제
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId){
        reservationService.deletePendingReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    // 비회원 예매 조회 (이메일 + 예매번호)
    @GetMapping("/guest")
    public ResponseEntity<ReservationDetailResponse> getNonMemberReservation(
            @RequestParam String email,
            @RequestParam String reservationCode) {
        
        ReservationDetailResponse reservationDetail = reservationService.getNonMemberReservationDetail(email, reservationCode);
        
        return ResponseEntity.ok(reservationDetail);
    }
}