package com.myce.advertisement.controller;

import com.myce.advertisement.service.ManageAdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/manage/ads")
@RequiredArgsConstructor
public class ManageAdvertisementController {
    private final ManageAdvertisementService service;

    @GetMapping("/check-available")
    public ResponseEntity<Void> checkAvailablePeriod(
            @RequestParam LocalDate displayStartDate,
            @RequestParam LocalDate displayEndDate,
            @RequestParam Long adPositionId){
        service.checkAvailablePeriod(adPositionId, displayStartDate, displayEndDate);
        return ResponseEntity.ok().build();
    }
}
