package com.myce.common.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;
import com.myce.common.service.ExpoAdminBusinessProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expo-admin/my-expo/profile")
@RequiredArgsConstructor
public class ExpoAdminBusinessProfileController {

    private final ExpoAdminBusinessProfileService service;

    @GetMapping//TODO:하위관리자
    public ResponseEntity<ExpoAdminBusinessProfileResponseDto> getMyBusinessProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        return ResponseEntity.ok(service.getExpoAdminBusinessProfile(memberId));
    }
}