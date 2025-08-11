package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.ad.AdvertisementDetailResponse;
import com.myce.member.dto.ad.AdvertisementPaymentDetailResponse;
import com.myce.member.dto.ad.AdvertisementRefundReceiptResponse;
import com.myce.member.dto.ad.MemberAdvertisementResponse;
import com.myce.member.service.MemberAdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/ads")
@RequiredArgsConstructor
public class MemberAdController {
    
    private final MemberAdService memberAdService;

    @GetMapping
    public ResponseEntity<List<MemberAdvertisementResponse>> getMemberAdvertisements(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        List<MemberAdvertisementResponse> advertisements = memberAdService.getMemberAdvertisements(memberId);
        
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
}