package com.myce.system.controller;

import com.myce.common.dto.PageResponse;
import com.myce.system.dto.adposition.*;
import com.myce.system.service.adposition.AdPositionService;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    
    // 광고 배너 위치 목록 조회 (크기 정보 포함)
    @GetMapping(value = "/dropdown-with-dimensions")
    public ResponseEntity<List<AdPositionDropdownWithDimensionsResponse>> getDropdownWithDimensions() {
        List<AdPositionDropdownWithDimensionsResponse> adPositions = adPositionService.getAdPositionDropdownWithDimensions();
        return ResponseEntity.ok().body(adPositions);
    }

    @GetMapping
    public PageResponse<AdPositionResponse> getAdPositionList(@RequestParam(defaultValue = "0") int page) {
        return adPositionService.getAdPositionList(page, PAGE_SIZE);
    }

    @GetMapping("/{bannerId}")
    public ResponseEntity<AdPositionDetailResponse> getAdPositionDetail(@PathVariable long bannerId) {
        return ResponseEntity.ok(adPositionService.getAdPositionDetail(bannerId));
    }

    @PutMapping("/{bannerId}/update")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<Void> updateAdPosition(@PathVariable long bannerId,
            @RequestBody @Valid AdPositionUpdateRequest request) {
        adPositionService.updateAdPosition(bannerId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/new")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<Void> newAdPosition(
            @RequestBody @Valid AdPositionNewRequest request){
        adPositionService.addAdPosition(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bannerId}/delete")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<Void> deleteAdPosition(@PathVariable long bannerId) {
        adPositionService.deleteAdPosition(bannerId);
        return ResponseEntity.ok().build();
    }
}
