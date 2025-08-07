package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.*;
import com.myce.member.service.MemberService;
import lombok.RequiredArgsConstructor;
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
    
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdrawMember(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        memberService.withdrawMember(memberId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-page/payment-history")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        Long memberId = customUserDetails.getMemberId();
        List<PaymentHistoryResponse> paymentHistory = memberService.getPaymentHistory(memberId);
        
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
}