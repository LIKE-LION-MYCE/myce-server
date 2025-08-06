package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.security.filter.JwtUtil;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.service.MyExpoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expos/my")
public class MyExpoController {

    private final MyExpoService expoService;
    private final JwtUtil jwtUtil;

    // 나의 박람회 상세 정보 조회
    @GetMapping("/{expoId}")
    public ResponseEntity<MyExpoDetailResponse> getMyExpoDetail(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        MyExpoDetailResponse response = expoService.getMyExpoDetail(expoId, memberId);
        return ResponseEntity.ok(response);
    }
}
