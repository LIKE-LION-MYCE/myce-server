package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.BoothRequest;
import com.myce.expo.dto.BoothResponse;
import com.myce.expo.service.BoothService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/booths")
@RequiredArgsConstructor
public class BoothController {

    private final BoothService boothService;

    // 부스 등록
    @PostMapping
    public ResponseEntity<BoothResponse> saveBooth(
            @PathVariable Long expoId,
            @Valid @RequestBody BoothRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BoothResponse response = boothService.saveBooth(expoId, request, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 해당 박람회의 부스 목록 조회
    @GetMapping
    public ResponseEntity<List<BoothResponse>> getMyBooths(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<BoothResponse> booths = boothService.getMyBooths(expoId, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.ok(booths);
    }

    // 부스 수정
    @PutMapping("/{boothId}")
    public ResponseEntity<BoothResponse> updateBooth(
            @PathVariable Long expoId,
            @PathVariable Long boothId,
            @Valid @RequestBody BoothRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BoothResponse response = boothService.updateBooth(expoId, boothId, request, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }

    // 부스 삭제
    @DeleteMapping("/{boothId}")
    public ResponseEntity<Void> deleteBooth(
            @PathVariable Long expoId,
            @PathVariable Long boothId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boothService.deleteBooth(expoId, boothId, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }
}