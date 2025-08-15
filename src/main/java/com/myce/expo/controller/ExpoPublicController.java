package com.myce.expo.controller;

import com.myce.system.dto.fee.ExpoFeeResponse;
import com.myce.system.service.fee.ExpoFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expo/fees")
public class ExpoPublicController {

    private final ExpoFeeService expoFeeService;

    @GetMapping("/active")
    public ResponseEntity<ExpoFeeResponse> getActiveExpoFee() {
        ExpoFeeResponse response = expoFeeService.getActiveExpoFee();
        return ResponseEntity.ok(response);
    }
}