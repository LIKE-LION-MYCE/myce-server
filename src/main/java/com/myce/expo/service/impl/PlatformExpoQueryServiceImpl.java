package com.myce.expo.service.impl;

import com.myce.common.dto.PageResponse;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.expo.dto.ExpoApplicationResponse;
import com.myce.expo.dto.ExpoApplicationDetailResponse;
import com.myce.expo.dto.ExpoRejectionInfoResponse;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.common.entity.RejectInfo;
import com.myce.common.repository.RejectInfoRepository;
import com.myce.expo.service.PlatformExpoQueryService;
import com.myce.expo.service.mapper.ExpoApplicationMapper;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.member.mapper.expo.ExpoPaymentDetailMapper;
import com.myce.member.mapper.expo.ExpoRefundReceiptMapper;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 플랫폼 관리자용 박람회 신청 조회 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformExpoQueryServiceImpl implements PlatformExpoQueryService {

    private final ExpoRepository expoRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final RejectInfoRepository rejectInfoRepository;
    private final ExpoPaymentDetailMapper expoPaymentDetailMapper;
    private final ExpoRefundReceiptMapper expoRefundReceiptMapper;

    @Override
    public PageResponse<ExpoApplicationResponse> getAllExpoApplications(
            int page, int pageSize, boolean latestFirst, String status) {
        

        Pageable pageable = createPageable(page, pageSize, latestFirst);
        Page<Expo> expos;
        
        if (status != null && !status.trim().isEmpty()) {
            ExpoStatus expoStatus = ExpoStatus.valueOf(status);
            expos = latestFirst ? 
                    expoRepository.findByStatusOrderByCreatedAtDesc(expoStatus, pageable) :
                    expoRepository.findByStatusOrderByCreatedAtAsc(expoStatus, pageable);
        } else {
            // 신청 관리 페이지는 신청 관련 상태들만 조회
            expos = expoRepository.findByStatusIn(ExpoStatus.APPLICATION_STATUSES, pageable);
        }
        
        return PageResponse.from(expos.map(ExpoApplicationMapper::toSimpleResponse));
    }

    @Override
    public PageResponse<ExpoApplicationResponse> getFilteredExpoApplicationsByKeyword(
            String keyword, String status, int page, int pageSize, boolean latestFirst) {
        

        Pageable pageable = createPageable(page, pageSize, latestFirst);
        Page<Expo> expos;
        
        if (status != null && !status.trim().isEmpty()) {
            ExpoStatus expoStatus = ExpoStatus.valueOf(status);
            expos = latestFirst ? 
                    expoRepository.findByTitleContainingAndStatusOrderByCreatedAtDesc(keyword, expoStatus, pageable) :
                    expoRepository.findByTitleContainingAndStatusOrderByCreatedAtAsc(keyword, expoStatus, pageable);
        } else {
            // 신청 관리 페이지 키워드 검색은 신청 관련 상태들만 조회
            expos = expoRepository.findByTitleContainingIgnoreCaseAndStatusIn(keyword, ExpoStatus.APPLICATION_STATUSES, pageable);
        }
        
        return PageResponse.from(expos.map(ExpoApplicationMapper::toSimpleResponse));
    }

    @Override
    public ExpoApplicationDetailResponse getExpoApplicationDetail(Long expoId) {
        
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new RuntimeException("박람회를 찾을 수 없습니다."));
        
        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElse(null);
        
        return ExpoApplicationMapper.toDetailResponse(expo, businessProfile);
    }

    @Override
    public ExpoPaymentDetailResponse getExpoPaymentInfo(Long expoId) {
        
        // 박람회 정보 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        // 사업자 정보 조회 (없을 수도 있음)
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElse(null);
        
        // 박람회 결제 정보 조회
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        return expoPaymentDetailMapper.toExpoPaymentDetailResponse(expo, businessProfile, expoPaymentInfo);
    }

    @Override
    public ExpoRejectionInfoResponse getExpoRejectionInfo(Long expoId) {
        
        // 거절 정보 조회
        RejectInfo rejectInfo = rejectInfoRepository.findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REJECT_INFO_NOT_FOUND));
        
        return ExpoRejectionInfoResponse.builder()
                .expoId(expoId)
                .reason(rejectInfo.getDescription())
                .rejectedAt(rejectInfo.getCreatedAt())
                .build();
    }

    @Override
    public ExpoRefundReceiptResponse getExpoCancelInfo(Long expoId) {
        
        // 박람회 정보 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        // 사업자 정보 조회 (없을 수도 있음)
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElse(null);
        
        // 박람회 결제 정보 조회 (환불 계산에 필요)
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        return expoRefundReceiptMapper.toRefundReceiptDto(expo, businessProfile, expoPaymentInfo);
    }

    @Override
    public PageResponse<ExpoApplicationResponse> getCurrentExpos(
            int page, int pageSize, boolean latestFirst, String status, String keyword) {
        

        Pageable pageable = createPageable(page, pageSize, latestFirst);
        
        // 상태별 필터링 (게시중: POSTING, 취소 대기: CANCEL_PENDING)
        Page<Expo> expos;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드 검색
            if (status != null) {
                ExpoStatus expoStatus = ExpoStatus.valueOf(status);
                expos = expoRepository.findByStatusAndTitleContainingIgnoreCase(expoStatus, keyword.trim(), pageable);
            } else {
                // 키워드 검색 시에도 운영중 상태들만 조회
                expos = expoRepository.findByTitleContainingIgnoreCaseAndStatusIn(keyword.trim(), ExpoStatus.ACTIVE_STATUSES, pageable);
            }
        } else {
            // 전체 조회 또는 상태 필터링
            if (status != null) {
                ExpoStatus expoStatus = ExpoStatus.valueOf(status);
                expos = expoRepository.findByStatus(expoStatus, pageable);
            } else {
                // 운영중 상태들만 조회
                expos = expoRepository.findByStatusIn(ExpoStatus.ACTIVE_STATUSES, pageable);
            }
        }


        return PageResponse.from(expos.map(ExpoApplicationMapper::toSimpleResponse));
    }

    private Pageable createPageable(int page, int pageSize, boolean latestFirst) {
        Sort sort = latestFirst ? 
                Sort.by("createdAt").descending() : 
                Sort.by("createdAt").ascending();
        return PageRequest.of(page, pageSize, sort);
    }
}