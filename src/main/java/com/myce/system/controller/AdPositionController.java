package com.myce.system.controller;

import com.myce.common.dto.PageResponse;
import com.myce.system.dto.adposition.AdPositionDropdownResponse;
import com.myce.system.dto.adposition.AdPositionResponse;
import com.myce.system.service.adposition.AdPositionService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ad-positions")
@RequiredArgsConstructor
public class AdPositionController {
    private final AdPositionService adPositionService;
    private final int PAGE_SIZE = 10;

    // 광고 배너 위치 목록 조회
    @GetMapping(value = "/dropdown")
    public ResponseEntity<List<AdPositionDropdownResponse>> getDropdown() {
        List<AdPositionDropdownResponse> adPositions = adPositionService.getAdPositionDropdown();
        return ResponseEntity.ok().body(adPositions);
    }

    @GetMapping
    public PageResponse<AdPositionResponse> getAdPositionList(@RequestParam int page) {
        return adPositionService.getAdPositionList(page, PAGE_SIZE);
    }
}
