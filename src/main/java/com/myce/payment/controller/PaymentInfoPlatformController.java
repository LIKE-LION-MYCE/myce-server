package com.myce.payment.controller;

import com.myce.common.dto.PageResponse;
import com.myce.payment.dto.PaymentInfoResponse;
import com.myce.payment.service.PaymentInfoPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/payment-info")
@RequiredArgsConstructor
public class PaymentInfoPlatformController {
    private final PaymentInfoPlatformService platformService;

    @GetMapping
    public PageResponse<PaymentInfoResponse> getPaymentInfoPage(
            @RequestParam(defaultValue = "true") boolean latestFirst) {
        return platformService.getPaymentInfoPage(1, 10, latestFirst);
    }

    @GetMapping("/filter")
    public PageResponse<PaymentInfoResponse> filterPaymentInfoPage(
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return platformService.filterPaymentInfoPage(1, 10, latestFirst, status, keyword);
    }
}
