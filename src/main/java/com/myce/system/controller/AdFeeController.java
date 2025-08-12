package com.myce.system.controller;

import com.myce.system.dto.fee.AdFeeListResponse;
import com.myce.system.dto.fee.AdFeeRequest;
import com.myce.system.service.fee.AdFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings/ad-fee")
public class AdFeeController {

    private final AdFeeService adFeeService;

    @PostMapping()
    public ResponseEntity<Void> save(@RequestBody @Valid AdFeeRequest request) {
        adFeeService.saveAdFee(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<AdFeeListResponse> findAll(
            @RequestParam(defaultValue = "0", required = false, name = "page")int page,
            @RequestParam(required = false, value = "position") Long positionId) {
        AdFeeListResponse response = adFeeService.getAdFeeList(page, positionId);
        return ResponseEntity.ok(response);
    }
}
