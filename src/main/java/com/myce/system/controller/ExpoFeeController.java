package com.myce.system.controller;

import com.myce.system.dto.fee.ExpoFeeListResponse;
import com.myce.system.dto.fee.ExpoFeeRequest;
import com.myce.system.dto.fee.ExpoFeeResponse;
import com.myce.system.dto.fee.FeeActiveRequest;
import com.myce.system.service.fee.ExpoFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public ResponseEntity<ExpoFeeListResponse> getExpoFeeSettings(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "name", required = false) String name) {
        ExpoFeeListResponse response = expoFeeService.getExpoFeeList(page, name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activation")
    public ResponseEntity<Void> updateActivation(@PathVariable Long id, @RequestBody FeeActiveRequest request) {
        expoFeeService.updateExpoFeeActivation(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<ExpoFeeResponse> getActiveExpoFee() {
        ExpoFeeResponse response = expoFeeService.getActiveExpoFee();
        return ResponseEntity.ok(response);
    }
}
