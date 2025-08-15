package com.myce.system.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.system.dto.email.ExpoAdminEmailDetailResponse;
import com.myce.system.dto.email.ExpoAdminEmailRequest;
import com.myce.system.dto.email.ExpoAdminEmailResponse;
import com.myce.system.service.email.ExpoAdminEmailDetailService;
import com.myce.system.service.email.ExpoAdminEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expos/{expoId}/emails")
@RequiredArgsConstructor
public class ExpoAdminEmailController {

    private final ExpoAdminEmailService service;
    private final ExpoAdminEmailDetailService detailService;

    @PostMapping
    public ResponseEntity<Void> sendEmail(
            @PathVariable Long expoId,
            @RequestBody @Valid ExpoAdminEmailRequest dto,
            @RequestParam(required = false) String entranceStatus,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String reservationCode,
            @RequestParam(required = false) String ticketName,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();
        service.sendMail(memberId,loginType,expoId,dto,entranceStatus,name,phone,reservationCode,ticketName);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<ExpoAdminEmailResponse>> getMyEmails(
            @PathVariable Long expoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page,size,Sort.by(direction,"createdAt"));

        Page<ExpoAdminEmailResponse> result = detailService.getMyMails(expoId, memberId, loginType, keyword, pageable);

        return ResponseEntity.ok(new PagedModel<>(result));
    }

    @GetMapping("/{emailId}")
    public ResponseEntity<ExpoAdminEmailDetailResponse> getMyEmailDetail(
            @PathVariable Long expoId,
            @PathVariable String emailId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        return ResponseEntity.ok(detailService.getMyMailDetail(expoId,memberId,loginType,emailId));
    }
}