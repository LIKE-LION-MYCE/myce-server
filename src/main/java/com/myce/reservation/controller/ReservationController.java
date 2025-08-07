package com.myce.reservation.controller;

import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    
    @GetMapping("/{reservationCode}")
    public ResponseEntity<ReservationDetailResponse> getReservationDetail(
            @PathVariable String reservationCode) {
        
        ReservationDetailResponse reservationDetail = reservationService.getReservationDetail(reservationCode);
        
        return ResponseEntity.ok(reservationDetail);
    }
}