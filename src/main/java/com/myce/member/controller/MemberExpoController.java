package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.*;
import com.myce.member.service.MemberExpoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberExpoController {
    
    private final MemberExpoService memberExpoService;

    @GetMapping("/my-page/expos")
    public ResponseEntity<Page<MemberExpoResponse>> getMemberExpos(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Pageable pageable) {
        
        Long memberId = customUserDetails.getMemberId();
        Page<MemberExpoResponse> expos = memberExpoService.getMemberExpos(memberId, pageable);
        
        return ResponseEntity.ok(expos);
    }
    
    @GetMapping("/my-page/expos/{expoId}")
    public ResponseEntity<MemberExpoDetailResponse> getMemberExpoDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        MemberExpoDetailResponse expoDetail = memberExpoService.getMemberExpoDetail(memberId, expoId);
        
        return ResponseEntity.ok(expoDetail);
    }
    
    @DeleteMapping("/my-page/expos/{expoId}")
    public ResponseEntity<Void> cancelExpo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberExpoService.cancelExpo(memberId, expoId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-page/expos/{expoId}/payment")
    public ResponseEntity<ExpoPaymentDetailResponse> getExpoPaymentDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoPaymentDetailResponse paymentDetail = memberExpoService.getExpoPaymentDetail(memberId, expoId);
        
        return ResponseEntity.ok(paymentDetail);
    }
    
    @GetMapping("/my-page/expos/{expoId}/admin-codes")
    public ResponseEntity<List<ExpoAdminCodeResponse>> getExpoAdminCodes(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        List<ExpoAdminCodeResponse> adminCodes = memberExpoService.getExpoAdminCodes(memberId, expoId);
        
        return ResponseEntity.ok(adminCodes);
    }
    
    @GetMapping("/my-page/expos/{expoId}/settlement-receipt")
    public ResponseEntity<ExpoSettlementReceiptResponse> getExpoSettlementReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoSettlementReceiptResponse settlementReceipt = memberExpoService.getExpoSettlementReceipt(memberId, expoId);
        
        return ResponseEntity.ok(settlementReceipt);
    }
    
    @GetMapping("/my-page/expos/{expoId}/refund-receipt")
    public ResponseEntity<ExpoRefundReceiptResponse> getExpoRefundReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoRefundReceiptResponse refundReceipt = memberExpoService.getExpoRefundReceipt(memberId, expoId);
        
        return ResponseEntity.ok(refundReceipt);
    }
}