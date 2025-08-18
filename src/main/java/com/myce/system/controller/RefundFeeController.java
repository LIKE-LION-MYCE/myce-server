package com.myce.system.controller;

import com.myce.system.dto.fee.FeeActiveRequest;
import com.myce.system.dto.fee.PublicRefundPolicyListResponse;
import com.myce.system.dto.fee.RefundFeeListResponse;
import com.myce.system.dto.fee.RefundFeeRequest;
import com.myce.system.dto.fee.UpdateRefundFeeRequest;
import com.myce.system.service.fee.RefundFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/settings/refund-fee")
public class RefundFeeController {

    private final RefundFeeService refundFeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<RefundFeeListResponse> getAllSettings(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "name", required = false) String name) {
        RefundFeeListResponse response = refundFeeService.getAllSettings(page, name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    public ResponseEntity<PublicRefundPolicyListResponse> getActivePublicRefundPolicy() {
        PublicRefundPolicyListResponse response = refundFeeService.getActivePublicRefundPolicy();

        // 디버깅을 위한 로그
        System.out.println("Public refund policy response: " + response.getPolicies().size() + " policies found");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public void saveRefundFee(@RequestBody @Valid RefundFeeRequest request) {
        refundFeeService.saveRefundFee(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public void updateRefundFee(@PathVariable("id") long id, @RequestBody @Valid UpdateRefundFeeRequest request) {
        refundFeeService.updateRefundFee(id, request);
    }

    @PutMapping("/{id}/activation")
    public ResponseEntity<Void> updateActivation(@PathVariable Long id, @RequestBody FeeActiveRequest request) {
        refundFeeService.updateRefundFeeActivation(id, request);
        return ResponseEntity.noContent().build();
    }
}