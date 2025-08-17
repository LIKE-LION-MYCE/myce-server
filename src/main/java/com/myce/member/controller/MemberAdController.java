package com.myce.member.controller;

import com.myce.advertisement.dto.AdRejectInfoResponse;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.ad.AdRefundReceiptResponse;
import com.myce.member.dto.ad.AdRefundRequest;
import com.myce.member.dto.ad.AdvertisementDetailResponse;
import com.myce.member.dto.ad.AdvertisementPaymentDetailResponse;
import com.myce.member.dto.ad.AdvertisementRefundReceiptResponse;
import com.myce.member.dto.ad.MemberAdvertisementResponse;
import com.myce.member.service.MemberAdService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/ads")
@RequiredArgsConstructor
public class MemberAdController {
    
    private final MemberAdService memberAdService;

    @GetMapping
    public ResponseEntity<Page<MemberAdvertisementResponse>> getMemberAdvertisements(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Long memberId = customUserDetails.getMemberId();
        Page<MemberAdvertisementResponse> advertisements = memberAdService.getMemberAdvertisements(memberId, pageable);
        
        return ResponseEntity.ok(advertisements);
    }
    
    @GetMapping("/{advertisementId}")
    public ResponseEntity<AdvertisementDetailResponse> getAdvertisementDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdvertisementDetailResponse advertisementDetail = memberAdService.getAdvertisementDetail(memberId, advertisementId);
        
        return ResponseEntity.ok(advertisementDetail);
    }
    
    @DeleteMapping("/{advertisementId}")
    public ResponseEntity<Void> cancelAdvertisement(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberAdService.cancelAdvertisement(memberId, advertisementId);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{advertisementId}/cancel-by-status")
    public ResponseEntity<Void> cancelByStatus(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberAdService.cancelByStatus(memberId, advertisementId);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{advertisementId}/refund-request-by-status")
    public ResponseEntity<Void> requestRefundByStatus(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId,
            @RequestBody AdRefundRequest request) {
        
        Long memberId = customUserDetails.getMemberId();
        memberAdService.requestRefundByStatus(memberId, advertisementId, request);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{advertisementId}/payment")
    public ResponseEntity<AdvertisementPaymentDetailResponse> getAdvertisementPaymentDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdvertisementPaymentDetailResponse paymentDetail = memberAdService.getAdvertisementPaymentDetail(memberId, advertisementId);
        
        return ResponseEntity.ok(paymentDetail);
    }
    
    @GetMapping("/{advertisementId}/refund-receipt")
    public ResponseEntity<AdvertisementRefundReceiptResponse> getAdvertisementRefundReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdvertisementRefundReceiptResponse refundReceipt = memberAdService.getAdvertisementRefundReceipt(memberId, advertisementId);
        
        return ResponseEntity.ok(refundReceipt);
    }
    
    @GetMapping("/{advertisementId}/reject-info")
    public ResponseEntity<AdRejectInfoResponse> getAdvertisementRejectInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdRejectInfoResponse rejectInfo = memberAdService.getAdvertisementRejectInfo(memberId, advertisementId);
        
        return ResponseEntity.ok(rejectInfo);
    }
    
    @GetMapping("/{advertisementId}/refund-history")
    public ResponseEntity<AdRefundReceiptResponse> getAdvertisementRefundHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdRefundReceiptResponse refundHistory = memberAdService.getAdvertisementRefundHistory(memberId, advertisementId);
        
        return ResponseEntity.ok(refundHistory);
    }
    
    @PostMapping("/{advertisementId}/payment/complete")
    public ResponseEntity<Void> completeAdvertisementPayment(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberAdService.completeAdvertisementPayment(memberId, advertisementId);
        
        return ResponseEntity.noContent().build();
    }
}