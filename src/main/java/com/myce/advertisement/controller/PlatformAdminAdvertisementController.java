package com.myce.advertisement.controller;

import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.FilterRequest;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.service.PlatformAdminAdvertisementService;
import com.myce.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public PageResponse<SimpleApplyAdvertisement> getApplyList(@RequestParam int page,
               @RequestParam(defaultValue = "true")
               boolean latestFirst) {
        return service.getAllApplyList(page, PAGE_SIZE,  latestFirst);

    @GetMapping("/filter")
    public PageResponse<SimpleApplyAdvertisement> filterApplyList(@RequestParam(defaultValue = "0") int page,
              @RequestParam(required = false) String status,
              @RequestParam(required = false) String keyword,
              @RequestParam(defaultValue = "true")
              boolean latestFirst) {
        return service.getFilteredApplyListByKeyword(keyword, status,
                page, PAGE_SIZE, latestFirst);
    public PageResponse<SimpleApplyAdvertisement> filterApplyList(@Valid FilterRequest request,
                @RequestParam(defaultValue = "true") boolean latestFirst) {
        return service.filterList(request.getKeyword(), request.getStatus(),
                request.getPage(), PAGE_SIZE, latestFirst);
    }

    @GetMapping("/detail/{bannerId}")
    public DetailApplyAdvertisement getApplyDetail(@PathVariable Long bannerId) {
        return service.getDetail(bannerId);
    }

}
