package com.myce.payment.controller;

import com.myce.common.dto.PageResponse;
import com.myce.payment.dto.PaymentInfoResponse;
import com.myce.payment.service.PaymentInfoPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/platform/payment-info")
@RequiredArgsConstructor
public class PaymentInfoPlatformController {
    private final PaymentInfoPlatformService platformService;
    private final int PAGE_SIZE = 10;

    @GetMapping
    public PageResponse<PaymentInfoResponse> getPaymentInfoPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "true") boolean latestFirst) {
        return platformService.getPaymentInfoPage(page, PAGE_SIZE, latestFirst);
    }

    @GetMapping("/filter")
    public PageResponse<PaymentInfoResponse> filterPaymentInfoPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return platformService.filterPaymentInfoPage(page, PAGE_SIZE, latestFirst, keyword, type,
                startDate, endDate);
    }
}
