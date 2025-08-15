package com.myce.advertisement.controller;

import com.myce.system.dto.fee.AdFeeResponse;
import com.myce.system.service.fee.AdFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ad/fees")
public class AdPublicController {

    private final AdFeeService adFeeService;

    @GetMapping("/active")
    public ResponseEntity<List<AdFeeResponse>> getActiveAdFees() {
        List<AdFeeResponse> responses = adFeeService.getActiveAdFees();
        return ResponseEntity.ok(responses);
    }
}