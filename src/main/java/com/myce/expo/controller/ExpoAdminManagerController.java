package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.ExpoAdminManagerResponse;
import com.myce.expo.service.ExpoAdminManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/managers")
@RequiredArgsConstructor
public class ExpoAdminManagerController {

    private final ExpoAdminManagerService service;

    @GetMapping//TODO:하위관리자
    public ResponseEntity<List<ExpoAdminManagerResponse>> getMyExpoManagers(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long memberId = userDetails.getMemberId();
        return ResponseEntity.ok(service.getMyExpoManagers(expoId,memberId));
    }
}
