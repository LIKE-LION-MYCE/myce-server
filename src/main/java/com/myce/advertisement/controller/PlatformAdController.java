package com.myce.advertisement.controller;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.service.PlatformAdDetailService;
import com.myce.advertisement.service.PlatformAdService;
import com.myce.advertisement.service.PlatformApplyAdService;
import com.myce.advertisement.service.PlatformCurrentAdService;
import com.myce.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/platform/ads")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
@Slf4j
public class PlatformAdController {
    private final PlatformAdService service;
    private final PlatformAdDetailService adDetailService;
    private final PlatformApplyAdService applyAdService;
    private final PlatformCurrentAdService currentAdService;

    private final int PAGE_SIZE = 10;

    @GetMapping
    public PageResponse<AdResponse> getAdList(
            @RequestParam int page,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(defaultValue = "true") boolean isApply) {
        return service.getAdList(page, PAGE_SIZE, latestFirst, isApply);
    }

    @GetMapping("/filter")
    public PageResponse<AdResponse> filterAdList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(defaultValue = "true") boolean isApply) {
        return service.getFilteredAdListByKeyword(keyword, status,
                page, PAGE_SIZE, latestFirst, isApply);
    }

    @GetMapping("/{adId}")
    public AdDetailResponse getApplyDetail(@PathVariable Long adId) {
        return adDetailService.getDetail(adId);
    }

    @PostMapping("/{adId}/approve")
    public ResponseEntity<Void> approveApply(@PathVariable Long adId) {
        applyAdService.approveApply(adId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{adId}/payment-check")
    public AdPaymentInfoCheck getPaymentForm(@PathVariable Long adId) {
        return applyAdService.generatePaymentCheck(adId);
    }

    @PostMapping("/{adId}/cancel")
    public ResponseEntity<Void> cancelApply(@PathVariable Long adId) {
        currentAdService.cancelCurrent(adId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{adId}/cancel/deny")
    public ResponseEntity<Void> denyCancel(@PathVariable Long adId) {
        currentAdService.denyCancel(adId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{adId}/cancel-check")
    public AdCancelInfoCheck getCancelForm(@PathVariable Long adId) {
        return currentAdService.generateCancelCheck(adId);
    }

    @PostMapping("/{adId}/reject")
    public ResponseEntity<Void> rejectApply(@PathVariable Long adId,
                                            @RequestBody AdRejectRequest request) {
        applyAdService.rejectApply(adId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{adId}/reject")
    public AdRejectInfoResponse getRejectInfo(@PathVariable Long adId) {
        return applyAdService.getRejectInfo(adId);
    }

    @GetMapping("/{adId}/payment-history")
    public AdPaymentHistoryResponse getPaymentHistory(@PathVariable Long adId) {
        return applyAdService.getPaymentHistory(adId);
    }

    @GetMapping("/{adId}/cancel-history")
    public AdCancelHistoryResponse getCancelHistory(@PathVariable Long adId) {
        return applyAdService.getCancelHistory(adId);
    }

    @PatchMapping("/{adId}/status")
    public ResponseEntity<Void> updateAdStatus(@PathVariable Long adId,
        @RequestBody AdStatusUpdateRequest request) {
        service.updateAdStatus(adId, request.getAdvertisementStatus());
        return ResponseEntity.ok().build();
    }
}
