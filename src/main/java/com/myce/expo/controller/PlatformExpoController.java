package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.dto.PageResponse;
import com.myce.expo.dto.*;
import com.myce.expo.service.PlatformExpoQueryService;
import com.myce.expo.service.PlatformExpoManageService;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.member.service.MemberExpoService;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.ExpoRepository;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 플랫폼 관리자용 박람회 신청 관리 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/platform/expo")
@PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
public class PlatformExpoController {

    private final PlatformExpoQueryService platformExpoQueryService;
    private final PlatformExpoManageService platformExpoManageService;
    private final MemberExpoService memberExpoService;
    private final ExpoRepository expoRepository;

    /**
     * 박람회 신청 목록 조회
     */
    @GetMapping
    public ResponseEntity<PageResponse<ExpoApplicationResponse>> getExpoApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(required = false) String status) {
        

        PageResponse<ExpoApplicationResponse> response = platformExpoQueryService
                .getAllExpoApplications(page, pageSize, latestFirst, status);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 박람회 신청 키워드 검색
     */
    @GetMapping("/filter")
    public ResponseEntity<PageResponse<ExpoApplicationResponse>> searchExpoApplications(
            @RequestParam String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "true") boolean latestFirst) {
        

        PageResponse<ExpoApplicationResponse> response = platformExpoQueryService
                .getFilteredExpoApplicationsByKeyword(keyword, status, page, pageSize, latestFirst);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 박람회 신청 상세 조회
     */
    @GetMapping("/{expoId}")
    public ResponseEntity<ExpoApplicationDetailResponse> getExpoApplicationDetail(
            @PathVariable Long expoId) {
        

        ExpoApplicationDetailResponse response = platformExpoQueryService
                .getExpoApplicationDetail(expoId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 박람회 승인 시 결제 정보 미리보기
     */
    @GetMapping("/{expoId}/payment-preview")
    public ResponseEntity<ExpoPaymentPreviewResponse> getPaymentPreview(@PathVariable Long expoId) {
        log.info("박람회 결제 정보 미리보기 요청 - expoId: {}", expoId);
        
        ExpoPaymentPreviewResponse response = platformExpoManageService.getPaymentPreview(expoId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 박람회 신청 승인
     */
    @PostMapping("/{expoId}/approve")
    public ResponseEntity<Void> approveExpoApplication(@PathVariable Long expoId) {
        

        platformExpoManageService.approveExpoApplication(expoId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 박람회 신청 거절
     */
    @PostMapping("/{expoId}/reject")
    public ResponseEntity<Void> rejectExpoApplication(
            @PathVariable Long expoId,
            @RequestBody ExpoRejectionRequest request) {
        

        platformExpoManageService.rejectExpoApplication(expoId, request.getReason());
        
        return ResponseEntity.ok().build();
    }

    /**
     * 박람회 결제 정보 조회
     */
    @GetMapping("/{expoId}/payment")
    public ResponseEntity<Object> getExpoPaymentInfo(@PathVariable Long expoId) {
        

        Object response = platformExpoQueryService.getExpoPaymentInfo(expoId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 박람회 거절 사유 조회
     */
    @GetMapping("/{expoId}/reject")
    public ResponseEntity<Object> getExpoRejectInfo(@PathVariable Long expoId) {
        

        Object response = platformExpoQueryService.getExpoRejectionInfo(expoId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 박람회 취소/환불 내역 조회
     */
    @GetMapping("/{expoId}/cancel")
    public ResponseEntity<Object> getExpoCancelInfo(@PathVariable Long expoId) {
        

        Object response = platformExpoQueryService.getExpoCancelInfo(expoId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 박람회 목록 조회 (게시중, 취소 대기)
     */
    @GetMapping("/current")
    public ResponseEntity<PageResponse<ExpoApplicationResponse>> getCurrentExpos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "true") boolean latestFirst,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        

        PageResponse<ExpoApplicationResponse> response = platformExpoQueryService
                .getCurrentExpos(page, pageSize, latestFirst, status, keyword);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 박람회 취소 승인
     */
    @PostMapping("/{expoId}/cancel-approve")
    public ResponseEntity<Void> approveCancellation(@PathVariable Long expoId) {
        
        
        platformExpoManageService.approveCancellation(expoId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 박람회 정산 승인
     */
    @PostMapping("/{expoId}/settlement-approve")
    public ResponseEntity<Void> approveSettlement(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long expoId) {
        
        Long adminMemberId = customUserDetails.getMemberId();
        log.info("박람회 정산 승인 요청 - expoId: {}, adminMemberId: {}", expoId, adminMemberId);
        
        platformExpoManageService.approveSettlement(expoId, adminMemberId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 박람회 정산 내역 조회 (플랫폼 관리자용)
     */
    @GetMapping("/{expoId}/settlement")
    public ResponseEntity<ExpoSettlementReceiptResponse> getExpoSettlement(@PathVariable Long expoId) {
        
        
        // 박람회 소유자의 memberId 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        Long memberId = expo.getMember().getId();
        ExpoSettlementReceiptResponse response = memberExpoService.getExpoSettlementReceipt(memberId, expoId);
        
        return ResponseEntity.ok(response);
    }
    // 박람회 관리자 목록 조회
    @GetMapping("/{expoId}/admin-info")
    public ResponseEntity<ExpoAdminInfoResponse> getExpoAdminInfo(@PathVariable Long expoId) {

        return ResponseEntity.ok(platformExpoQueryService.getExpoAdminInfo(expoId));
    }
}