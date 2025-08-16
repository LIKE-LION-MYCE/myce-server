package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.*;
import com.myce.member.dto.expo.FavoriteExpoResponse;
import com.myce.member.dto.expo.ReservedExpoResponse;
import com.myce.member.service.MemberMyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/my-page")
@RequiredArgsConstructor
public class MemberMyPageController {
    
    private final MemberMyPageService memberMyPageService;
    
    @GetMapping("/reserved-expos")
    public ResponseEntity<Page<ReservedExpoResponse>> getReservedExpos(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Pageable pageable) {
        
        Long memberId = customUserDetails.getMemberId();
        Page<ReservedExpoResponse> reservedExpos = memberMyPageService.getReservedExpos(memberId, pageable);
        
        return ResponseEntity.ok(reservedExpos);
    }

    @GetMapping("/favorite-expos")
    public ResponseEntity<List<FavoriteExpoResponse>> getFavoriteExpos(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long memberId = customUserDetails.getMemberId();
        List<FavoriteExpoResponse> favoriteExpos = memberMyPageService.getFavoriteExpos(memberId);

        return ResponseEntity.ok(favoriteExpos);
    }
    
    @GetMapping("/info")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        MemberInfoResponse memberInfo = memberMyPageService.getMemberInfo(memberId);
        
        return ResponseEntity.ok(memberInfo);
    }
    
    @PutMapping("/info")
    public ResponseEntity<Void> updateMemberInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MemberInfoUpdateRequest request) {
        
        Long memberId = customUserDetails.getMemberId();
        memberMyPageService.updateMemberInfo(memberId, request);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payment-history")
    public ResponseEntity<Page<PaymentHistoryResponse>> getPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Pageable pageable) {
        
        Long memberId = customUserDetails.getMemberId();
        Page<PaymentHistoryResponse> paymentHistory = memberMyPageService.getPaymentHistory(memberId, pageable);
        
        return ResponseEntity.ok(paymentHistory);
    }
    
    @GetMapping("/settings")
    public ResponseEntity<MemberSettingResponse> getMemberSetting(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        MemberSettingResponse memberSetting = memberMyPageService.getMemberSetting(memberId);
        
        return ResponseEntity.ok(memberSetting);
    }
    
    @PutMapping("/settings")
    public ResponseEntity<Void> updateMemberSetting(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MemberSettingUpdateRequest request) {
        
        Long memberId = customUserDetails.getMemberId();
        memberMyPageService.updateMemberSetting(memberId, request);
        
        return ResponseEntity.ok().build();
    }
}