package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.expo.ExpoAdminCodeResponse;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementRequest;
import com.myce.member.dto.expo.MemberExpoDetailResponse;
import com.myce.member.dto.expo.MemberExpoResponse;
import com.myce.member.service.MemberExpoService;
import com.myce.refund.dto.RefundRequestDto;
import com.myce.refund.service.RefundRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/expos")
@RequiredArgsConstructor
public class MemberExpoController {
    
    private final MemberExpoService memberExpoService;
    private final RefundRequestService refundRequestService;

    @GetMapping
    public ResponseEntity<Page<MemberExpoResponse>> getMemberExpos(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Pageable pageable) {
        
        Long memberId = customUserDetails.getMemberId();
        Page<MemberExpoResponse> expos = memberExpoService.getMemberExpos(memberId, pageable);
        
        return ResponseEntity.ok(expos);
    }
    
    @GetMapping("/{expoId}")
    public ResponseEntity<MemberExpoDetailResponse> getMemberExpoDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        MemberExpoDetailResponse expoDetail = memberExpoService.getMemberExpoDetail(memberId, expoId);
        
        return ResponseEntity.ok(expoDetail);
    }
    
    @DeleteMapping("/{expoId}")
    public ResponseEntity<Void> cancelExpo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberExpoService.cancelExpo(memberId, expoId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{expoId}/payment")
    public ResponseEntity<ExpoPaymentDetailResponse> getExpoPaymentDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoPaymentDetailResponse paymentDetail = memberExpoService.getExpoPaymentDetail(memberId, expoId);
        
        return ResponseEntity.ok(paymentDetail);
    }
    
    @GetMapping("/{expoId}/admin-codes")
    public ResponseEntity<List<ExpoAdminCodeResponse>> getExpoAdminCodes(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        List<ExpoAdminCodeResponse> adminCodes = memberExpoService.getExpoAdminCodes(memberId, expoId);
        
        return ResponseEntity.ok(adminCodes);
    }
    
    @GetMapping("/{expoId}/settlement-receipt")
    public ResponseEntity<ExpoSettlementReceiptResponse> getExpoSettlementReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoSettlementReceiptResponse settlementReceipt = memberExpoService.getExpoSettlementReceipt(memberId, expoId);
        
        return ResponseEntity.ok(settlementReceipt);
    }
    
    @GetMapping("/{expoId}/refund-receipt")
    public ResponseEntity<ExpoRefundReceiptResponse> getExpoRefundReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        System.out.println("===== MemberExpoController.getExpoRefundReceipt 호출 - expoId: " + expoId + " =====");
        
        Long memberId = customUserDetails.getMemberId();
        ExpoRefundReceiptResponse refundReceipt = memberExpoService.getExpoRefundReceipt(memberId, expoId);
        
        System.out.println("===== 응답 refundAmount: " + refundReceipt.getRefundAmount() + " =====");
        
        return ResponseEntity.ok(refundReceipt);
    }
    
    @GetMapping("/{expoId}/refund-history")
    public ResponseEntity<ExpoRefundReceiptResponse> getExpoRefundHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoRefundReceiptResponse refundHistory = memberExpoService.getExpoRefundHistory(memberId, expoId);
        
        return ResponseEntity.ok(refundHistory);
    }
    
    @PostMapping("/{expoId}/settlement")
    public ResponseEntity<Void> requestExpoSettlement(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId,
            @RequestBody ExpoSettlementRequest request) {
        
        Long memberId = customUserDetails.getMemberId();
        memberExpoService.requestExpoSettlement(memberId, expoId, request);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{expoId}/payment-complete")
    public ResponseEntity<Void> completeExpoPayment(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberExpoService.completeExpoPayment(memberId, expoId);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{expoId}/refund-request")
    public ResponseEntity<Void> requestExpoRefund(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId,
            @Valid @RequestBody RefundRequestDto requestDto) {

        Long memberId = customUserDetails.getMemberId();
        refundRequestService.createRefundRequest(memberId, expoId, requestDto);
        
        return ResponseEntity.ok().build();
    }
}