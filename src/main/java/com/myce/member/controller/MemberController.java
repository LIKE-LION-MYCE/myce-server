package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.dto.ReservedExpoResponse;
import com.myce.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}