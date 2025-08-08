package com.myce.advertisement.controller;

import com.myce.advertisement.dto.AdRejectInfoResponse;
import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.service.PlatformAdminAdvertisementService;
import com.myce.common.dto.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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

    @PostMapping("/detail/{bannerId}/reject")
    public ResponseEntity<Void> rejectApply(@PathVariable Long bannerId,
                                            @RequestParam String reason) {
        service.rejectApply(bannerId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{bannerId}/reject")
    public AdRejectInfoResponse getRejectInfo(@PathVariable Long bannerId) {
        return service.getRejectInfo(bannerId);
    }

}
