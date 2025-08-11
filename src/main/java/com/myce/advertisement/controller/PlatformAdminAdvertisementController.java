package com.myce.advertisement.controller;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.service.PlatformAdminAdvertisementService;
import com.myce.common.dto.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/platform/ads/list")
public class PlatformAdminAdvertisementController {
    @Autowired
    private PlatformAdminAdvertisementService service;

    private final int PAGE_SIZE = 10;

    @GetMapping
    public PageResponse<SimpleApplyAdvertisement> getAdvertisementList(
            @RequestParam int page,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(defaultValue = "true") boolean isApply) {
        return service.getAllAdList(page, PAGE_SIZE, latestFirst, isApply);
    }

    @GetMapping("/filter")
    public PageResponse<SimpleApplyAdvertisement> filterAdvertisementList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(defaultValue = "true") boolean isApply) {
        return service.getFilteredAdListByKeyword(keyword, status,
                page, PAGE_SIZE, latestFirst, isApply);
    }

    @GetMapping("/detail/{bannerId}")
    public DetailApplyAdvertisement getApplyDetail(@PathVariable Long bannerId) {
        return service.getDetail(bannerId);
    }

    @PostMapping("/detail/{bannerId}/approve")
    public ResponseEntity<Void> approveApply(@PathVariable Long bannerId,
                                             @RequestBody AdPaymentInfoRequest paymentInfoRequest) {
        service.approveApply(bannerId, paymentInfoRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{bannerId}/payment-check")
    public AdPaymentInfoCheck getPaymentForm(@PathVariable Long bannerId) {
        return service.generatePaymentCheck(bannerId);
    }

    @PostMapping("/detail/{bannerId}/cancel")
    public ResponseEntity<Void> cancelApply(@PathVariable Long bannerId,
                                            @RequestBody AdCancelInfoRequest request) {
        service.cancelBanner(bannerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{bannerId}/cancel-check")
    public AdCancelInfoCheck getCancelForm(@PathVariable Long bannerId) {
        return service.generateCancelCheck(bannerId);
    }

    @PostMapping("/detail/{bannerId}/reject")
    public ResponseEntity<Void> rejectApply(@PathVariable Long bannerId,
                                            @RequestBody RejectAdRequest request) {
        service.rejectApply(bannerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{bannerId}/reject")
    public AdRejectInfoResponse getRejectInfo(@PathVariable Long bannerId) {
        return service.getRejectInfo(bannerId);
    }

    @GetMapping("/detail/{bannerId}/payment-history")
    public AdPaymentHistoryResponse getPaymentInfo(@PathVariable Long bannerId) {
        return service.getPaymentInfo(bannerId);
    }

    @GetMapping("/detail/{bannerId}/cancel-history")
    public AdCancelHistoryResponse getCancelInfo(@PathVariable Long bannerId) {
        return service.getCancelInfo(bannerId);
    }

}
