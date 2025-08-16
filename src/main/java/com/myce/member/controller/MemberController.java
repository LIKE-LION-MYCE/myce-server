package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.dto.MemberInfoListResponse;
import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.dto.MemberInfoWithMileageResponse;
import com.myce.member.dto.MileageUpdateRequest;
import com.myce.member.dto.PasswordChangeRequest;
import com.myce.member.service.MemberGradeService;
import com.myce.member.service.MemberMileageService;
import com.myce.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    private final MemberMileageService memberMileageService;
    private final MemberGradeService memberGradeService;

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdrawMember(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        memberService.withdrawMember(memberId);
        
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody  @Valid PasswordChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        memberService.changePassword(memberId, request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<MemberInfoListResponse> getMemberByRole(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam String role) {
        log.debug("[getMemberByRole] {}", role);
        MemberInfoListResponse response = memberService.getMemberInfoByRole(page, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-info")
    public ResponseEntity<MemberInfoWithMileageResponse> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();

        return ResponseEntity.ok(memberService.getMyInfo(memberId));
    }

    @GetMapping("/my-mileage")
    public ResponseEntity<Integer> getMyMileage(
        @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();

        return  ResponseEntity.ok(memberService.getMyMileage(memberId));
    }

    @PatchMapping("/my-mileage")
    public ResponseEntity<Void> updateMileageForReservation(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody  @Valid MileageUpdateRequest request
        ){
        Long memberId = customUserDetails.getMemberId();
        memberMileageService.updateMileageForReservation(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/grade")
    public ResponseEntity<Void> updateGrade(
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        Long memberId = customUserDetails.getMemberId();
        memberGradeService.udpateGrade(memberId);
        return ResponseEntity.noContent().build();
    }
}