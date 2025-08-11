package com.myce.advertisement.controller;

import com.myce.advertisement.dto.AdMainPageInfo;
import com.myce.advertisement.service.SystemAdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class SystemAdController {
    private final SystemAdService service;

    @GetMapping("/check-available")
    public ResponseEntity<Void> checkAvailablePeriod(
            @RequestParam LocalDate displayStartDate,
            @RequestParam LocalDate displayEndDate,
            @RequestParam Long adPositionId){
        service.checkAvailablePeriod(adPositionId, displayStartDate, displayEndDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AdMainPageInfo>> getAds(){
        return ResponseEntity.ok(service.getActiveAds());
    }
}
