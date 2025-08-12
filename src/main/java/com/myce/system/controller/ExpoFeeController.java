package com.myce.system.controller;

import com.myce.system.dto.fee.ExpoFeeRequest;
import com.myce.system.service.fee.ExpoFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings/expo-fee")
public class ExpoFeeController {

    private final ExpoFeeService expoFeeService;

    @PostMapping
    public ResponseEntity<Void> save (@RequestBody @Valid ExpoFeeRequest request) {
        expoFeeService.saveExpoFee(request);
        return ResponseEntity.noContent().build();
    }
}
