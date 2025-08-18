package com.myce.expo.service.impl;

import com.myce.common.dto.PageResponse;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.expo.dto.*;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.common.entity.RejectInfo;
import com.myce.common.repository.RejectInfoRepository;
import com.myce.expo.service.PlatformExpoQueryService;
import com.myce.expo.service.mapper.ExpoApplicationMapper;
import com.myce.expo.service.mapper.ExpoCancelDetailMapper;
import com.myce.expo.service.mapper.PlatformExpoMapper;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.member.mapper.expo.ExpoPaymentDetailMapper;
import com.myce.member.mapper.expo.ExpoRefundReceiptMapper;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.member.entity.Member;
import com.myce.member.entity.Guest;
import com.myce.member.repository.MemberRepository;
import com.myce.member.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

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
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final ExpoPaymentDetailMapper expoPaymentDetailMapper;
    private final ExpoRefundReceiptMapper expoRefundReceiptMapper;
    
    // 개별 예약자 환불 정보 조회용 의존성
    private final ReservationRepository reservationRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final MemberRepository memberRepository;
    private final GuestRepository guestRepository;

    private final AdminCodeRepository adminCodeRepository;

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
    public ExpoCancelDetailResponse getExpoCancelInfo(Long expoId) {
        log.info("박람회 취소 상세 정보 조회 시작 - expoId: {}", expoId);
        
        // 1. 박람회 정보 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        // 2. 사업자 정보 조회 (없을 수도 있음)
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElse(null);
        
        // 3. 박람회 결제 정보 조회
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        // 4. 박람회 주최자 환불 정보 조회
        Payment expoPayment = paymentRepository.findByTargetIdAndTargetType(expoId, PaymentTargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        // 원래 환불 신청 정보 조회 (가장 처음 신청된 것)
        List<Refund> refunds = refundRepository.findAll().stream()
                .filter(refund -> refund.getPayment().equals(expoPayment))
                .sorted((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt())) // 생성일 오름차순
                .toList();
        
        if (refunds.isEmpty()) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_FOUND);
        }
        
        Refund expoRefund = refunds.get(0); // 원래 환불 신청 정보 사용
        
        // 5. 개별 예약자 정보 조회
        List<Reservation> reservations = reservationRepository.findByExpoId(expoId);
        
        // 박람회 상태에 따라 다른 예약 필터링 적용
        List<Reservation> targetReservations;
        if (expo.getStatus() == ExpoStatus.CANCELLED) {
            // 취소 완료된 박람회: 환불된 예약들(CANCELLED)도 포함하여 표시
            targetReservations = reservations.stream()
                    .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED 
                                        || reservation.getStatus() == ReservationStatus.CANCELLED)
                    .toList();
        } else {
            // 취소 대기 상태: 확정된 예약만 표시
            targetReservations = reservations.stream()
                    .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED)
                    .toList();
        }
        
        // 6. 개별 예약자 환불 정보 생성
        List<ExpoCancelDetailResponse.IndividualReservationRefund> reservationRefunds = new ArrayList<>();
        Integer totalReservationAmount = 0;
        
        for (Reservation reservation : targetReservations) {
            // 예약 결제 정보 조회
            ReservationPaymentInfo reservationPaymentInfo = reservationPaymentInfoRepository
                    .findByReservationId(reservation.getId())
                    .orElse(null);
            
            if (reservationPaymentInfo != null) {
                // 예약자 정보 조회
                String reserverName = getReserverName(reservation);
                
                ExpoCancelDetailResponse.IndividualReservationRefund refundInfo = 
                    ExpoCancelDetailResponse.IndividualReservationRefund.builder()
                        .reservationCode(reservation.getReservationCode())
                        .reserverName(reserverName)
                        .ticketName(reservation.getTicket().getName())
                        .quantity(reservation.getQuantity())
                        .refundAmount(reservationPaymentInfo.getTotalAmount())
                        .userType(reservation.getUserType().name())
                        .build();
                
                reservationRefunds.add(refundInfo);
                totalReservationAmount += reservationPaymentInfo.getTotalAmount();
            }
        }
        
        // 7. 환불 날짜 포맷팅
        String refundRequestDate = expoRefund.getRefundedAt() != null ? 
                expoRefund.getRefundedAt().toLocalDate().toString() : 
                expoRefund.getCreatedAt().toLocalDate().toString();
        
        // 8. 응답 DTO 구성 (Mapper 사용)
        Integer usedAmount = expoRefund.getIsPartial() ? calculateUsedAmount(expo, expoPaymentInfo) : 0;
        Integer usedDays = expoRefund.getIsPartial() ? calculateUsedDays(expo) : 0;
        
        ExpoCancelDetailResponse response = ExpoCancelDetailMapper.toResponse(
                expo,
                businessProfile,
                expoPaymentInfo,
                expoRefund,
                refundRequestDate,
                usedAmount,
                usedDays,
                targetReservations.size(),
                totalReservationAmount,
                reservationRefunds
        );
        
        log.info("박람회 취소 상세 정보 조회 완료 - expoId: {}, 예약자 수: {}, 총 예약자 환불액: {}", 
                expoId, targetReservations.size(), totalReservationAmount);
        
        return response;
    }
    
    /**
     * 예약자 이름 조회 (회원/비회원 구분)
     */
    private String getReserverName(Reservation reservation) {
        if (reservation.getUserType() == UserType.MEMBER) {
            return memberRepository.findById(reservation.getUserId())
                    .map(Member::getName)
                    .orElse("알 수 없음");
        } else {
            return guestRepository.findById(reservation.getUserId())
                    .map(Guest::getName)
                    .orElse("알 수 없음");
        }
    }
    
    /**
     * 사용한 금액 계산 (부분환불인 경우)
     */
    private Integer calculateUsedAmount(Expo expo, ExpoPaymentInfo paymentInfo) {
        // 사용한 일수 기반으로 이용료 계산
        int usedDays = calculateUsedDays(expo);
        return usedDays * paymentInfo.getDailyUsageFee();
    }
    
    /**
     * 사용한 일수 계산 (부분환불인 경우)  
     */
    private Integer calculateUsedDays(Expo expo) {
        LocalDate today = LocalDate.now();
        LocalDate displayStartDate = expo.getDisplayStartDate();
        
        // 게시 시작일부터 오늘까지의 일수 계산 (시작일 포함)
        int usedDays = (int) java.time.temporal.ChronoUnit.DAYS.between(displayStartDate, today) + 1;
        
        // 게시 시작 전이면 0일
        if (usedDays < 0) usedDays = 0;
        
        return usedDays;
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

    @Override
    public ExpoAdminInfoResponse getExpoAdminInfo(Long expoId) {
        List<AdminCode> subAdmins = adminCodeRepository.findByExpoId(expoId);
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        return PlatformExpoMapper.getExpoAdminInfoResponse(expo, subAdmins);
    }
}