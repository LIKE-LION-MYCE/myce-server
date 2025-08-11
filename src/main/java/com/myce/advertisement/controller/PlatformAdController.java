package com.myce.advertisement.controller;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.service.PlatformAdDetailService;
import com.myce.advertisement.service.PlatformAdService;
import com.myce.advertisement.service.PlatformApplyAdService;
import com.myce.advertisement.service.PlatformCurrentAdService;
import com.myce.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/platform/ads/list")
@RequiredArgsConstructor
public class PlatformAdController {
    private final PlatformAdService service;
    private final PlatformAdDetailService adDetailService;
    private final PlatformApplyAdService applyAdService;
    private final PlatformCurrentAdService currentAdService;

    private final int PAGE_SIZE = 10;

    @GetMapping
    public PageResponse<AdSimpleResponse> getAdvertisementList(
            @RequestParam int page,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(defaultValue = "true") boolean isApply) {
        return service.getAllAdList(page, PAGE_SIZE, latestFirst, isApply);
    }

    @GetMapping("/filter")
    public PageResponse<AdSimpleResponse> filterAdvertisementList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(defaultValue = "true") boolean isApply) {
        return service.getFilteredAdListByKeyword(keyword, status,
                page, PAGE_SIZE, latestFirst, isApply);
    }

    @GetMapping("/detail/{bannerId}")
    public AdDetailResponse getApplyDetail(@PathVariable Long bannerId) {
        return adDetailService.getDetail(bannerId);
    }

    @PostMapping("/detail/{bannerId}/approve")
    public ResponseEntity<Void> approveApply(@PathVariable Long bannerId,
                                             @RequestBody AdPaymentInfoRequest paymentInfoRequest) {
        applyAdService.approveApply(bannerId, paymentInfoRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{bannerId}/payment-check")
    public AdPaymentInfoCheck getPaymentForm(@PathVariable Long bannerId) {
        return applyAdService.generatePaymentCheck(bannerId);
    }

    @PostMapping("/detail/{bannerId}/cancel")
    public ResponseEntity<Void> cancelApply(@PathVariable Long bannerId,
                                            @RequestBody AdCancelInfoRequest request) {
        currentAdService.cancelBanner(bannerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{bannerId}/cancel-check")
    public AdCancelInfoCheck getCancelForm(@PathVariable Long bannerId) {
        return currentAdService.generateCancelCheck(bannerId);
    }

    @PostMapping("/detail/{bannerId}/reject")
    public ResponseEntity<Void> rejectApply(@PathVariable Long bannerId,
                                            @RequestBody AdRejectRequest request) {
        applyAdService.rejectApply(bannerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{bannerId}/reject")
    public AdRejectInfoResponse getRejectInfo(@PathVariable Long bannerId) {
        return applyAdService.getRejectInfo(bannerId);
    }

    @GetMapping("/detail/{bannerId}/payment-history")
    public AdPaymentHistoryResponse getPaymentHistory(@PathVariable Long bannerId) {
        return applyAdService.getPaymentHistory(bannerId);
    }

    @GetMapping("/detail/{bannerId}/cancel-history")
    public AdCancelHistoryResponse getCancelHistory(@PathVariable Long bannerId) {
        return applyAdService.getCancelHistory(bannerId);
    }

}
