package com.myce.advertisement.controller;

import com.myce.advertisement.dto.FilterRequest;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.service.PlatformAdminAdvertisementService;
import com.myce.common.dto.PageResponse;
import com.myce.common.exception.CustomException;
import com.myce.common.exception.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/platform/advertisement/list")
public class PlatformAdminAdvertisementController {
    @Autowired
    private PlatformAdminAdvertisementService service;

    private final int PAGE_SIZE = 10;

    @GetMapping()
    public PageResponse<SimpleApplyAdvertisement> getApplyList(@RequestParam int page, @RequestParam(defaultValue = "true") boolean latestFirst) {
        return service.getList(page, PAGE_SIZE,  latestFirst);
    }

    @GetMapping("/filter")
    public PageResponse<SimpleApplyAdvertisement> filterApplyList(@Valid FilterRequest request, @RequestParam(defaultValue = "true") boolean latestFirst) {
        return service.filterList(request.getKeyword(), request.getStatus(), request.getPage(), PAGE_SIZE, latestFirst);
    }

}
