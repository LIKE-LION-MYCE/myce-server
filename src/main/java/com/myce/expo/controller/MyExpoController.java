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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expos/my")
public class MyExpoController {

    private final MyExpoService expoService;

    // 관리자 페이지로 이동 가능한 박람회 리스트 조회
    @GetMapping
    public ResponseEntity<List<Long>> getMyExpos(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        return ResponseEntity.ok(expoService.getMyExpos(memberId));
    }

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
