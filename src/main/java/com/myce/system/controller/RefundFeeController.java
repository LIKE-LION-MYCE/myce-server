package com.myce.system.controller;

import com.myce.system.dto.fee.RefundFeeListResponse;
import com.myce.system.service.fee.RefundFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings/refund-fee")
public class RefundFeeController {

    private final RefundFeeService refundFeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<RefundFeeListResponse> getAllSettings(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page) {
        RefundFeeListResponse response = refundFeeService.getAllSettings(page);
        return ResponseEntity.ok(response);
    }

}