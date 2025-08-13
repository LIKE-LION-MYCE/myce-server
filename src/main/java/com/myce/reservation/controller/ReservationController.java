package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPendingRequest;
import com.myce.reservation.dto.ReservationSuccessResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;
import com.myce.reservation.dto.ResolveReserversRequest;
import com.myce.reservation.dto.ResolveReserversResponse;
import com.myce.reservation.service.ReservationService;
import com.myce.reservation.service.ReserverResolveService;
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
    private final ReserverResolveService reserverResolveService;

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

    /**
     * 입력받은 reserverInfos를 회원/게스트 식별 + Guest upsert까지 수행해
     * 결제 검증 단계에서 사용할 식별자(memberId/guestId)를 채워 반환
     */
    @PostMapping("/resolvers")
    public ResponseEntity<ResolveReserversResponse> resolveReservers(
        @Valid @RequestBody ResolveReserversRequest request
    ) {
        ResolveReserversResponse response = reserverResolveService.resolve(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pending")
    public ResponseEntity<Long> saveReservationPending(
        @Valid @RequestBody ReservationPendingRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.saveReservationPending(request));
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
}