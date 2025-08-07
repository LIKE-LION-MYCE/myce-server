package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.BoothRegistrationRequest;
import com.myce.expo.dto.BoothRegistrationResponse;
import com.myce.expo.service.BoothService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expos/{expoId}/booths")
@RequiredArgsConstructor
public class BoothController {

    private final BoothService boothService;

    // 부스 등록
    @PostMapping
    public ResponseEntity<BoothRegistrationResponse> saveBooth(
            @PathVariable Long expoId,
            @Valid @RequestBody BoothRegistrationRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        BoothRegistrationResponse response = boothService.saveBooth(expoId, request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
