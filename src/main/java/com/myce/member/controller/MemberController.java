package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.*;
import com.myce.member.dto.ExpoPaymentDetailResponse;
import com.myce.member.service.MemberService;
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
public class MemberController {
    
    private final MemberService memberService;
    
    @GetMapping("/my-page/reserved-expos")
    public ResponseEntity<List<ReservedExpoResponse>> getReservedExpos(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        List<ReservedExpoResponse> reservedExpos = memberService.getReservedExpos(memberId);
        
        return ResponseEntity.ok(reservedExpos);
    }

    @GetMapping("/my-page/favorite_expos")
    public ResponseEntity<List<FavoriteExpoResponse>> getFavoriteExpos(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long memberId = customUserDetails.getMemberId();
        List<FavoriteExpoResponse> favoriteExpos = memberService.getFavoriteExpos(memberId);

        return ResponseEntity.ok(favoriteExpos);
    }
    
    @GetMapping("/my-page/info")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        MemberInfoResponse memberInfo = memberService.getMemberInfo(memberId);
        
        return ResponseEntity.ok(memberInfo);
    }
    
    @PutMapping("/my-page/info")
    public ResponseEntity<Void> updateMemberInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MemberInfoUpdateRequest request) {
        
        Long memberId = customUserDetails.getMemberId();
        memberService.updateMemberInfo(memberId, request);
        
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdrawMember(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        memberService.withdrawMember(memberId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-page/payment-history")
    public ResponseEntity<Page<PaymentHistoryResponse>> getPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Pageable pageable) {
        
        Long memberId = customUserDetails.getMemberId();
        Page<PaymentHistoryResponse> paymentHistory = memberService.getPaymentHistory(memberId, pageable);
        
        return ResponseEntity.ok(paymentHistory);
    }
    
    @GetMapping("/my-page/settings")
    public ResponseEntity<MemberSettingResponse> getMemberSetting(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        MemberSettingResponse memberSetting = memberService.getMemberSetting(memberId);
        
        return ResponseEntity.ok(memberSetting);
    }
    
    @PutMapping("/my-page/settings")
    public ResponseEntity<Void> updateMemberSetting(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MemberSettingUpdateRequest request) {
        
        Long memberId = customUserDetails.getMemberId();
        memberService.updateMemberSetting(memberId, request);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my-page/advertisements")
    public ResponseEntity<List<MemberAdvertisementResponse>> getMemberAdvertisements(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        List<MemberAdvertisementResponse> advertisements = memberService.getMemberAdvertisements(memberId);
        
        return ResponseEntity.ok(advertisements);
    }
    
    @GetMapping("/my-page/advertisements/{advertisementId}")
    public ResponseEntity<AdvertisementDetailResponse> getAdvertisementDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdvertisementDetailResponse advertisementDetail = memberService.getAdvertisementDetail(memberId, advertisementId);
        
        return ResponseEntity.ok(advertisementDetail);
    }
    
    @DeleteMapping("/my-page/advertisements/{advertisementId}")
    public ResponseEntity<Void> cancelAdvertisement(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberService.cancelAdvertisement(memberId, advertisementId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-page/advertisements/{advertisementId}/payment")
    public ResponseEntity<AdvertisementPaymentDetailResponse> getAdvertisementPaymentDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdvertisementPaymentDetailResponse paymentDetail = memberService.getAdvertisementPaymentDetail(memberId, advertisementId);
        
        return ResponseEntity.ok(paymentDetail);
    }
    
    @GetMapping("/my-page/advertisements/{advertisementId}/refund-receipt")
    public ResponseEntity<AdvertisementRefundReceiptResponse> getAdvertisementRefundReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long advertisementId) {
        
        Long memberId = customUserDetails.getMemberId();
        AdvertisementRefundReceiptResponse refundReceipt = memberService.getAdvertisementRefundReceipt(memberId, advertisementId);
        
        return ResponseEntity.ok(refundReceipt);
    }
    
    @GetMapping("/my-page/expos")
    public ResponseEntity<List<MemberExpoResponse>> getMemberExpos(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        List<MemberExpoResponse> expos = memberService.getMemberExpos(memberId);
        
        return ResponseEntity.ok(expos);
    }
    
    @GetMapping("/my-page/expos/{expoId}")
    public ResponseEntity<MemberExpoDetailResponse> getMemberExpoDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        MemberExpoDetailResponse expoDetail = memberService.getMemberExpoDetail(memberId, expoId);
        
        return ResponseEntity.ok(expoDetail);
    }
    
    @DeleteMapping("/my-page/expos/{expoId}")
    public ResponseEntity<Void> cancelExpo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        memberService.cancelExpo(memberId, expoId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-page/expos/{expoId}/payment")
    public ResponseEntity<ExpoPaymentDetailResponse> getExpoPaymentDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoPaymentDetailResponse paymentDetail = memberService.getExpoPaymentDetail(memberId, expoId);
        
        return ResponseEntity.ok(paymentDetail);
    }
    
    @GetMapping("/my-page/expos/{expoId}/admin-codes")
    public ResponseEntity<List<ExpoAdminCodeResponse>> getExpoAdminCodes(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        List<ExpoAdminCodeResponse> adminCodes = memberService.getExpoAdminCodes(memberId, expoId);
        
        return ResponseEntity.ok(adminCodes);
    }
    
    @GetMapping("/my-page/expos/{expoId}/settlement-receipt")
    public ResponseEntity<ExpoSettlementReceiptResponse> getExpoSettlementReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoSettlementReceiptResponse settlementReceipt = memberService.getExpoSettlementReceipt(memberId, expoId);
        
        return ResponseEntity.ok(settlementReceipt);
    }
    
    @GetMapping("/my-page/expos/{expoId}/refund-receipt")
    public ResponseEntity<ExpoRefundReceiptResponse> getExpoRefundReceipt(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long memberId = customUserDetails.getMemberId();
        ExpoRefundReceiptResponse refundReceipt = memberService.getExpoRefundReceipt(memberId, expoId);
        
        return ResponseEntity.ok(refundReceipt);
    }
}