package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoUpdateRequest;
import com.myce.expo.service.MyExpoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expos/my")
public class MyExpoController {

    private final MyExpoService expoService;

    // 나의 박람회 상세 정보 조회
    @GetMapping("/{expoId}")
    public ResponseEntity<MyExpoDetailResponse> getMyExpoDetail(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        MyExpoDetailResponse response = expoService.getMyExpoDetail(expoId, memberId);
        return ResponseEntity.ok(response);
    }

    // 나의 박람회 상세 정보 수정
    @PutMapping("/{expoId}")
    public ResponseEntity<MyExpoDetailResponse> updateMyExpoDetail(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody MyExpoUpdateRequest updateRequest) {
        Long memberId = customUserDetails.getMemberId();
        MyExpoDetailResponse updatedExpo = expoService.updateMyExpoDetail(expoId, memberId, updateRequest); // 서비스에서 DTO 반환
        return ResponseEntity.ok(updatedExpo); // 업데이트된 DTO 반환
    }
}
