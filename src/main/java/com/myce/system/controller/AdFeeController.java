package com.myce.system.controller;

import com.myce.system.dto.fee.AdFeeListResponse;
import com.myce.system.dto.fee.AdFeeRequest;
import com.myce.system.dto.fee.AdFeeResponse;
import com.myce.system.dto.fee.FeeActiveRequest;
import com.myce.system.service.fee.AdFeeService;

import java.util.List;
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
@RequestMapping("/api/settings/ad-fee")
public class AdFeeController {

    private final AdFeeService adFeeService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody @Valid AdFeeRequest request) {
        adFeeService.saveAdFee(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<AdFeeListResponse> getAdFeeSettings(
            @RequestParam(name = "page", defaultValue = "0", required = false)int page,
            @RequestParam(value = "position", required = false) Long positionId,
            @RequestParam(value = "name", required = false) String name) {
        AdFeeListResponse response = adFeeService.getAdFeeList(page, positionId, name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activation")
    public ResponseEntity<Void> updateActivation(@PathVariable Long id, @RequestBody FeeActiveRequest request) {
        adFeeService.updateAdFeeActivation(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<AdFeeResponse>> getActiveAdFees() {
        List<AdFeeResponse> responses = adFeeService.getActiveAdFees();
        return ResponseEntity.ok(responses);
    }
}
