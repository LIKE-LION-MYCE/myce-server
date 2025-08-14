package com.myce.reservation.controller;

import com.myce.reservation.dto.ReserverBulkSaveRequest;
import com.myce.reservation.dto.ReserverUpdateRequest;
import com.myce.reservation.service.ReserverService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping()
    public ResponseEntity<Void> saveReservers(
        @RequestBody @Valid ReserverBulkSaveRequest request){
        reserverService.saveReservers(request.getReservationId(), request.getReserverInfos());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}