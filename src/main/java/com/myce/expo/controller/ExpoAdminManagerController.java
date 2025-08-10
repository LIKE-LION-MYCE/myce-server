package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.ExpoAdminManagerRequest;
import com.myce.expo.dto.ExpoAdminManagerResponse;
import com.myce.expo.service.ExpoAdminManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/managers")
@RequiredArgsConstructor
public class ExpoAdminManagerController {

    private final ExpoAdminManagerService service;

    @GetMapping
    public ResponseEntity<List<ExpoAdminManagerResponse>> getMyExpoManagers(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long memberId = userDetails.getMemberId();
        LoginType loginType = userDetails.getLoginType();
        return ResponseEntity.ok(service.getMyExpoManagers(expoId,memberId,loginType));
    }

    @PutMapping
    public ResponseEntity<List<ExpoAdminManagerResponse>> updateMyExpoManagers(
            @PathVariable Long expoId,
            @RequestBody List<ExpoAdminManagerRequest> dtos,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long memberId = userDetails.getMemberId();
        LoginType loginType = userDetails.getLoginType();
        return ResponseEntity.ok(service.updateMyExpoManagers(expoId,memberId,loginType,dtos));
    }
}