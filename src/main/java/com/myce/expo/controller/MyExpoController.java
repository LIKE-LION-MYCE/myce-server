package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.ExpoAdminPermissionResponse;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoDescriptionUpdateRequest;
import com.myce.expo.service.MyExpoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expos/my")
public class MyExpoController {

    private final MyExpoService expoService;

    //로그인한 사용자 기반 박람회 및 박람회 세부 접근권한 반환
    @GetMapping
    public ResponseEntity<ExpoAdminPermissionResponse> getExpoAdminPermission(
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();
        ExpoAdminPermissionResponse response = expoService.getExpoAdminPermission(memberId, loginType);
        return ResponseEntity.ok(response);
    }

    // 나의 박람회 상세 정보 조회
    @GetMapping("/{expoId}")
    public ResponseEntity<MyExpoDetailResponse> getMyExpoDetail(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        MyExpoDetailResponse response = expoService.getMyExpoDetail(expoId, customUserDetails.getLoginType(), customUserDetails.getMemberId());
        return ResponseEntity.ok(response);
    }


    // 나의 박람회 설명 부분 수정 (PENDING_PUBLISH 상태용)
    @PatchMapping("/{expoId}/description")
    public ResponseEntity<MyExpoDetailResponse> updateMyExpoDescription(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MyExpoDescriptionUpdateRequest updateRequest) {
        MyExpoDetailResponse updatedExpo = expoService.updateMyExpoDescription(expoId, updateRequest, customUserDetails.getLoginType(), customUserDetails.getMemberId());
        return ResponseEntity.ok(updatedExpo);
    }
}
