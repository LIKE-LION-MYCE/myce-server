package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.reservation.dto.ExpoAdminEmailRequest;
import com.myce.reservation.service.ExpoAdminEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expos/{expoId}/emails")
@RequiredArgsConstructor
public class ExpoAdminMailController {

    private final ExpoAdminEmailService service;

    @PostMapping
    public ResponseEntity<Void> sendEmail(
            @PathVariable Long expoId,
            @RequestBody @Valid ExpoAdminEmailRequest dto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();
        service.sendMail(memberId,loginType,expoId,dto);
        return ResponseEntity.ok().build();
    }
}
