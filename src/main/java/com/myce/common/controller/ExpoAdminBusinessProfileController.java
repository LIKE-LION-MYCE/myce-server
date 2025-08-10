package com.myce.common.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.common.dto.ExpoAdminBusinessProfileRequestDto;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;
import com.myce.common.service.ExpoAdminBusinessProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expos/{expoId}/profile")
@RequiredArgsConstructor
public class ExpoAdminBusinessProfileController {

    private final ExpoAdminBusinessProfileService service;

    @GetMapping
    public ResponseEntity<ExpoAdminBusinessProfileResponseDto> getMyBusinessProfile(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();
        return ResponseEntity.ok(service.getMyBusinessProfile(expoId,memberId,loginType));
    }

    @PutMapping
    public ResponseEntity<ExpoAdminBusinessProfileResponseDto> updateMyBusinessProfile(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ExpoAdminBusinessProfileRequestDto dto) {
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();
        return ResponseEntity.ok(service.updateMyBusinessProfile(expoId,memberId,loginType,dto));
    }
}