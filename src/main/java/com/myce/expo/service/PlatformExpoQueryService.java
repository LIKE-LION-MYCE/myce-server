package com.myce.expo.service;

import com.myce.common.dto.PageResponse;
import com.myce.expo.dto.*;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;

/**
 * 플랫폼 관리자용 박람회 신청 조회 서비스 인터페이스
 */
public interface PlatformExpoQueryService {

    /**
     * 박람회 신청 목록 조회 (전체)
     */
    PageResponse<ExpoApplicationResponse> getAllExpoApplications(
            int page, int pageSize,
            boolean latestFirst, String status);

    /**
     * 박람회 신청 목록 조회 (필터링)
     */
    PageResponse<ExpoApplicationResponse> getFilteredExpoApplicationsByKeyword(
            String keyword, String status,
            int page, int pageSize, boolean latestFirst);

    /**
     * 박람회 신청 상세 조회
     */
    ExpoApplicationDetailResponse getExpoApplicationDetail(Long expoId);
    
    /**
     * 박람회 결제 정보 조회 (플랫폼 관리자용)
     */
    ExpoPaymentDetailResponse getExpoPaymentInfo(Long expoId);
    
    /**
     * 박람회 거절 사유 조회 (플랫폼 관리자용)
     */
    ExpoRejectionInfoResponse getExpoRejectionInfo(Long expoId);
    
    /**
     * 박람회 취소/환불 내역 조회 (플랫폼 관리자용)
     * 박람회 주최자 환불 정보 + 개별 예약자 환불 정보 포함
     */
    ExpoCancelDetailResponse getExpoCancelInfo(Long expoId);

    /**
     * 현재 박람회 목록 조회 (게시중, 취소 대기)
     */
    PageResponse<ExpoApplicationResponse> getCurrentExpos(
            int page, int pageSize,
            boolean latestFirst, String status, String keyword);

    ExpoAdminInfoResponse getExpoAdminInfo(Long expoId);
}