package com.myce.reservation.controller;

import com.myce.reservation.dto.ReserverUpdateRequest;
import com.myce.reservation.service.ReserverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservers")
@RequiredArgsConstructor
public class ReserverController {
    
    private final ReserverService reserverService;
    
    @PutMapping("/{reserverId}")
    public ResponseEntity<Void> updateReserver(
            @PathVariable Long reserverId,
            @RequestBody @Valid ReserverUpdateRequest requestDto) {
        
        reserverService.updateReserver(reserverId, requestDto);
        
        return ResponseEntity.ok().build();
    }
}