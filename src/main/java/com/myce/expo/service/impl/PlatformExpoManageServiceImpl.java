package com.myce.expo.service.impl;

import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.RejectInfo;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.repository.RejectInfoRepository;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.PlatformExpoManageService;
import com.myce.expo.service.mapper.ExpoPaymentInfoMapper;
import com.myce.expo.service.mapper.RejectInfoMapper;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.service.refund.PaymentRefundService;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.expo.dto.ExpoPaymentPreviewResponse;
import com.myce.expo.service.mapper.ExpoPaymentPreviewMapper;
import com.myce.settlement.service.SettlementPlatformAdminService;
import com.myce.system.entity.ExpoFeeSetting;
import com.myce.system.repository.ExpoFeeSettingRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.TicketRepository;
import com.myce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 플랫폼 관리자용 박람회 신청 관리 서비스 구현체
 * 박람회 승인/거절 처리를 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformExpoManageServiceImpl implements PlatformExpoManageService {

    // 상수 정의
    private static final String CANCELLATION_REFUND_REASON = "박람회 취소 승인으로 인한 전액 환불";
    private static final String INDIVIDUAL_RESERVATION_REFUND_REASON = "박람회 취소로 인한 개인 예약 환불";
    
    // 의존성 주입
    private final ExpoRepository expoRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final RejectInfoRepository rejectInfoRepository;
    private final ExpoFeeSettingRepository expoFeeSettingRepository;
    private final SettlementPlatformAdminService settlementPlatformAdminService;
    private final MemberRepository memberRepository;
    private final BusinessProfileRepository businessProfileRepository;
    
    // 취소/환불 관련 의존성
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentRefundService paymentRefundService;
    
    // 개인 예약자 환불 관련 의존성
    private final ReservationRepository reservationRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final TicketRepository ticketRepository;
    
    // 알림 서비스
    private final NotificationService notificationService;

    /**
     * 박람회 신청 승인 처리
     * - 박람회 상태를 PENDING_APPROVAL -> PENDING_PAYMENT로 변경
     * - ExpoPaymentInfo 생성하여 결제 대기 상태로 설정
     * 
     * @param expoId 승인할 박람회 ID
     */
    @Override
    @Transactional
    public void approveExpoApplication(Long expoId) {
        // 1. 요청 정보 로깅
        
        // 2. 박람회 엔티티 조회 및 검증
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 박람회 ID로 승인 시도: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 3. 활성화된 수수료 설정 조회
        ExpoFeeSetting feeSetting = expoFeeSettingRepository.findByIsActiveTrue()
                .orElseThrow(() -> {
                    log.error("활성화된 박람회 수수료 설정이 존재하지 않음");
                    return new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND);
                });
        
        // 4. 박람회 일수 및 총 금액 계산
        int totalDays = calculateTotalDays(expo);
        Integer totalAmount = calculateTotalAmount(expo, feeSetting, totalDays);
        
        
        // 5. ExpoPaymentInfo 생성 (Mapper 사용)
        ExpoPaymentInfo paymentInfo = ExpoPaymentInfoMapper.toEntity(expo, feeSetting, totalDays, totalAmount);
        
        expoPaymentInfoRepository.save(paymentInfo);
        
        // 6. 박람회 상태 변경 (Entity 메서드 사용)
        String oldStatus = expo.getStatus().name();
        expo.approve();
        String newStatus = expo.getStatus().name();
        
        // 7. 상태 변경 알림 전송
        try {
            notificationService.sendExpoStatusChangeNotification(expoId, expo.getTitle(), oldStatus, newStatus);
        } catch (Exception e) {
            log.warn("박람회 승인 알림 전송 실패 - expoId: {}, 오류: {}", expoId, e.getMessage());
        }
        
        log.info("박람회 신청 승인 완료 - expoId: {}, totalAmount: {}", expoId, totalAmount);
    }

    /**
     * 박람회 신청 거절 처리
     * - 박람회 상태를 PENDING_APPROVAL -> REJECTED로 변경
     * - RejectInfo 생성하여 거절 사유 저장
     * 
     * @param expoId 거절할 박람회 ID
     * @param reason 거절 사유
     */
    @Override
    public void rejectExpoApplication(Long expoId, String reason) {
        // 1. 요청 정보 로깅
        
        // 2. 박람회 엔티티 조회 및 검증
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 박람회 ID로 거절 시도: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 3. 거절 정보 생성 (Mapper 사용)
        RejectInfo rejectInfo = RejectInfoMapper.toEntity(expo.getId(), reason);
        
        rejectInfoRepository.save(rejectInfo);
        
        // 4. 박람회 상태 변경 (Entity 메서드 사용)
        String oldStatus = expo.getStatus().name();
        expo.reject();
        String newStatus = expo.getStatus().name();
        
        // 5. 상태 변경 알림 전송
        try {
            notificationService.sendExpoStatusChangeNotification(expoId, expo.getTitle(), oldStatus, newStatus);
        } catch (Exception e) {
            log.warn("박람회 거절 알림 전송 실패 - expoId: {}, 오류: {}", expoId, e.getMessage());
        }
        
        log.info("박람회 신청 거절 완료 - expoId: {}", expoId);
    }

    /**
     * 박람회 취소 승인 처리
     * - PENDING_CANCEL 상태의 박람회를 CANCELLED로 변경
     * - 원래 상태(PUBLISHED/PENDING_PUBLISH)에 따라 다른 환불 처리
     * - PENDING_PUBLISH: 박람회 주최자만 전액 환불
     * - PUBLISHED: 개별 예약자 환불 + 박람회 주최자 부분 환불
     * 
     * @param expoId 취소 승인할 박람회 ID
     */
    @Override
    @Transactional
    public void approveCancellation(Long expoId) {
        // 1. 요청 정보 로깅
        log.info("박람회 취소 승인 요청 - expoId: {}", expoId);
        
        // 2. 박람회 조회 및 상태 검증
        Expo expo = validateExpoForCancellation(expoId);
        
        // 3. 결제 정보 조회
        ExpoPaymentInfo paymentInfo = getExpoPaymentInfo(expoId);
        
        // 4. 환불 신청 정보로 원래 상태 확인
        boolean wasPublishedStatus = determineOriginalExpoStatus(expoId);
        
        if (wasPublishedStatus) {
            // 5-1. PUBLISHED 상태였던 경우: 개별 예약자 환불 + 부분 환불
            log.info("PUBLISHED 상태 박람회 취소 승인 처리 - expoId: {}", expoId);
            
            // 개별 예약자들 환불 처리 (선처리)
            processIndividualReservationRefunds(expoId);
            
            // 박람회 주최자 부분 환불 처리
            processRefundToPortOne(expoId, paymentInfo);
            
            // 박람회 상태 변경
            String oldStatus = expo.getStatus().name();
            expo.approveCancellation();
            String newStatus = expo.getStatus().name();
            
            // 상태 변경 알림 전송
            try {
                notificationService.sendExpoStatusChangeNotification(expoId, expo.getTitle(), oldStatus, newStatus);
            } catch (Exception e) {
                log.warn("박람회 취소 승인 알림 전송 실패 - expoId: {}, 오류: {}", expoId, e.getMessage());
            }
            
            // 결제 정보 상태를 PARTIAL_REFUNDED로 변경 (개별 예약자 환불이 있었으므로)
            paymentInfo.setStatus(PaymentStatus.PARTIAL_REFUNDED);
            
        } else {
            // 5-2. PENDING_PUBLISH 상태였던 경우: 기존 로직 그대로 (전액 환불)
            log.info("PENDING_PUBLISH 상태 박람회 취소 승인 처리 - expoId: {}", expoId);
            
            // 포트원 환불 처리
            processRefundToPortOne(expoId, paymentInfo);
            
            // 박람회 상태 변경 (PENDING_CANCEL -> CANCELLED)
            String oldStatus = expo.getStatus().name();
            expo.approveCancellation();
            String newStatus = expo.getStatus().name();
            
            // 상태 변경 알림 전송
            try {
                notificationService.sendExpoStatusChangeNotification(expoId, expo.getTitle(), oldStatus, newStatus);
            } catch (Exception e) {
                log.warn("박람회 취소 승인 알림 전송 실패 - expoId: {}, 오류: {}", expoId, e.getMessage());
            }
            
            // 결제 정보 상태 변경 (SUCCESS -> REFUNDED)
            paymentInfo.setStatus(PaymentStatus.REFUNDED);
        }
        
        log.info("박람회 취소 승인 완료 - expoId: {}, 원래상태: {}", 
                expoId, wasPublishedStatus ? "PUBLISHED" : "PENDING_PUBLISH");
    }
    
    /**
     * 박람회 취소 승인 가능 여부 검증
     * 
     * @param expoId 박람회 ID
     * @return 검증된 박람회 엔티티
     */
    private Expo validateExpoForCancellation(Long expoId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("박람회 취소 승인 실패 - 존재하지 않는 박람회 ID: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        if (expo.getStatus() != ExpoStatus.PENDING_CANCEL) {
            log.error("박람회 취소 승인 실패 - 잘못된 상태: {}, expoId: {}", expo.getStatus(), expoId);
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
        
        return expo;
    }
    
    /**
     * 박람회 결제 정보 조회
     * 
     * @param expoId 박람회 ID
     * @return 박람회 결제 정보
     */
    private ExpoPaymentInfo getExpoPaymentInfo(Long expoId) {
        return expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> {
                    log.error("박람회 취소 승인 실패 - 결제 정보 없음: expoId {}", expoId);
                    return new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND);
                });
    }
    
    /**
     * 포트원 환불 처리 및 Refund 테이블 생성
     * 
     * @param expoId 박람회 ID
     * @param paymentInfo 박람회 결제 정보
     */
    private void processRefundToPortOne(Long expoId, ExpoPaymentInfo paymentInfo) {
        // 1. Payment 엔티티 조회
        Payment payment = paymentRepository.findByTargetIdAndTargetType(expoId, PaymentTargetType.EXPO)
                .orElseThrow(() -> {
                    log.error("박람회 취소 승인 실패 - Payment 정보 없음: expoId {}", expoId);
                    return new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND);
                });
        
        // 2. 기존 환불 신청 조회 및 상태 업데이트
        Refund pendingRefund = refundRepository.findByPayment(payment)
                .orElseThrow(() -> {
                    log.error("박람회 취소 승인 실패 - 환불 신청 정보 없음: expoId {}", expoId);
                    return new CustomException(CustomErrorCode.REFUND_NOT_FOUND);
                });
        
        // 3. 포트원 환불 요청 생성
        PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .cancelAmount(null)  // null = 전액 환불
                .reason(CANCELLATION_REFUND_REASON)
                .build();
        
        try {
            // 4. 포트원 환불 API 호출
            paymentRefundService.refundPayment(refundRequest);
            log.info("포트원 환불 처리 완료 - expoId: {}, amount: {}", expoId, paymentInfo.getTotalAmount());
            
        } catch (Exception e) {
            log.error("포트원 환불 처리 실패 - expoId: {}, error: {}", expoId, e.getMessage());
            throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
        }
    }

    /**
     * 박람회 정산 승인 처리
     * - Settlement 로직을 SettlementPlatformAdminService로 완전 위임
     * 
     * @param expoId 정산 승인할 박람회 ID
     * @param adminMemberId 현재 로그인한 플랫폼 관리자 ID
     */
    @Override
    @Transactional
    public void approveSettlement(Long expoId, Long adminMemberId) {
        log.info("박람회 정산 승인 요청 - expoId: {}, adminMemberId: {}", expoId, adminMemberId);
        
        // Settlement 로직은 SettlementPlatformAdminService로 완전 위임
        settlementPlatformAdminService.approveSettlement(expoId, adminMemberId);
        
        log.info("박람회 정산 승인 위임 완료 - expoId: {}, adminMemberId: {}", expoId, adminMemberId);
    }

    /**
     * 박람회 총 일수 계산
     * 게시 기간(displayStartDate ~ displayEndDate) 기준으로 계산
     * 
     * @param expo 박람회 엔티티
     * @return 총 일수
     */
    private int calculateTotalDays(Expo expo) {
        long days = ChronoUnit.DAYS.between(expo.getDisplayStartDate(), expo.getDisplayEndDate()) + 1;
        return (int) days;
    }

    /**
     * 박람회 총 금액 계산
     * 보증금 + (일일사용료 × 일수) + 프리미엄보증금
     * 
     * @param expo 박람회 엔티티
     * @param feeSetting 수수료 설정
     * @param totalDays 총 일수
     * @return 총 금액
     */
    private Integer calculateTotalAmount(Expo expo, ExpoFeeSetting feeSetting, int totalDays) {
        Integer deposit = feeSetting.getDeposit() != null ? feeSetting.getDeposit() : 0;
        Integer dailyUsageFee = feeSetting.getDailyUsageFee() != null ? feeSetting.getDailyUsageFee() : 0;
        Integer dailyFee = dailyUsageFee * totalDays;
        
        if (expo.getIsPremium()) {
            // 프리미엄일 경우: 기본 등록금 + 프리미엄 이용료 + 사용료
            Integer premiumDeposit = feeSetting.getPremiumDeposit() != null ? feeSetting.getPremiumDeposit() : 0;
            return deposit + premiumDeposit + dailyFee;
        } else {
            // 기본일 경우: 기본 등록금 + 사용료
            return deposit + dailyFee;
        }
    }
    
    /**
     * 박람회 승인 시 결제 정보 미리보기
     * DB 저장 없이 계산된 결제 정보를 반환
     * 팀 코드 스타일: 체계적인 로깅과 예외 처리
     * 
     * @param expoId 박람회 ID
     * @return 결제 정보 미리보기 응답
     */
    @Override
    @Transactional(readOnly = true)
    public ExpoPaymentPreviewResponse getPaymentPreview(Long expoId) {
        // 1. 요청 정보 로깅
        log.debug("결제 정보 미리보기 요청 - expoId: {}", expoId);
        
        // 2. 박람회 엔티티 조회 및 검증
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 박람회 ID로 결제 정보 미리보기 시도: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 3. 상태 검증 (PENDING_APPROVAL 상태에서만 미리보기 가능)
        if (expo.getStatus() != ExpoStatus.PENDING_APPROVAL) {
            log.warn("잘못된 상태에서 결제 정보 미리보기 시도 - expoId: {}, status: {}", expoId, expo.getStatus());
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
        
        // 4. 활성화된 수수료 설정 조회
        ExpoFeeSetting feeSetting = expoFeeSettingRepository.findByIsActiveTrue()
                .orElseThrow(() -> {
                    log.error("활성화된 박람회 수수료 설정이 존재하지 않음");
                    return new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND);
                });
        
        // 5. 사업자 정보 조회 (optional)
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElse(null);
        
        // 6. 기존 private 메서드 활용하여 계산
        int totalDays = calculateTotalDays(expo);
        Integer totalAmount = calculateTotalAmount(expo, feeSetting, totalDays);
        
        log.debug("결제 정보 계산 완료 - expoId: {}, totalDays: {}, totalAmount: {}", 
                expoId, totalDays, totalAmount);
        
        // 7. Mapper를 통해 DTO 변환
        return ExpoPaymentPreviewMapper.toDto(expo, businessProfile, feeSetting, totalDays, totalAmount);
    }
    
    /**
     * 게시중 박람회 취소 시 개인 예약자들 환불 처리
     * PUBLISHED 상태 박람회 취소 승인 시 모든 예약자에게 개별 환불 처리
     * 
     * @param expoId 취소 승인할 박람회 ID
     */
    private void processIndividualReservationRefunds(Long expoId) {
        log.info("개인 예약자 환불 처리 시작 - expoId: {}", expoId);
        
        // 1. 해당 박람회의 모든 예약 조회 후 CONFIRMED 상태만 필터링
        List<Reservation> allReservations = reservationRepository.findByExpoId(expoId);
        List<Reservation> confirmedReservations = allReservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED)
                .toList();
        
        if (confirmedReservations.isEmpty()) {
            log.info("환불 처리할 예약이 없음 - expoId: {}", expoId);
            return;
        }
        
        log.info("환불 처리 대상 예약 수: {} - expoId: {}", confirmedReservations.size(), expoId);
        
        // 2. 각 예약별 환불 처리
        for (Reservation reservation : confirmedReservations) {
            try {
                refundIndividualReservation(reservation);
                log.debug("개별 예약 환불 완료 - reservationId: {}, expoId: {}", 
                        reservation.getId(), expoId);
            } catch (Exception e) {
                log.error("개별 예약 환불 실패 - reservationId: {}, expoId: {}, error: {}", 
                        reservation.getId(), expoId, e.getMessage());
                throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
            }
        }
        
        log.info("개인 예약자 환불 처리 완료 - expoId: {}, 처리된 예약 수: {}", 
                expoId, confirmedReservations.size());
    }
    
    /**
     * 개별 예약자 환불 처리
     * 예약 상태 변경, 결제 정보 환불 처리, 포트원 환불 API 호출, 티켓 재고 복구
     * 
     * @param reservation 환불 처리할 예약 정보
     */
    private void refundIndividualReservation(Reservation reservation) {
        Long reservationId = reservation.getId();
        log.debug("개별 예약 환불 처리 시작 - reservationId: {}", reservationId);
        
        // 1. 예약 상태를 CANCELLED로 변경
        reservation.updateStatus(ReservationStatus.CANCELLED);
        log.debug("예약 상태 변경 완료 - reservationId: {}, status: CANCELLED", reservationId);
        
        // 2. 예약 결제 정보 조회 및 상태 변경
        ReservationPaymentInfo paymentInfo = reservationPaymentInfoRepository
                .findByReservationId(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 결제 정보 없음 - reservationId: {}", reservationId);
                    return new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND);
                });
        
        // 3. 결제 정보 상태를 REFUNDED로 변경
        paymentInfo.setStatus(PaymentStatus.REFUNDED);
        log.debug("예약 결제 정보 상태 변경 완료 - reservationId: {}, status: REFUNDED", reservationId);
        
        // 4. Payment 엔티티 조회
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(reservationId, PaymentTargetType.RESERVATION)
                .orElseThrow(() -> {
                    log.error("Payment 정보 없음 - reservationId: {}", reservationId);
                    return new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND);
                });

        // 5. refund 생성하지 않아도 됨. 환불 API 호출하면서 생성!
        
        // 6. 포트원 환불 API 호출
        PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .cancelAmount(null)  // null = 전액 환불
                .reason(INDIVIDUAL_RESERVATION_REFUND_REASON)
                .build();
        
        try {
            paymentRefundService.refundPayment(refundRequest);
            log.debug("포트원 환불 처리 완료 - reservationId: {}, amount: {}", 
                    reservationId, paymentInfo.getTotalAmount());
        } catch (Exception e) {
            log.error("포트원 환불 처리 실패 - reservationId: {}, error: {}", reservationId, e.getMessage());
            throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
        }
        
        // 7. 티켓 재고 복구
        restoreTicketInventory(reservation);
        
        log.debug("개별 예약 환불 처리 완료 - reservationId: {}", reservationId);
    }
    
    /**
     * 티켓 재고 복구 처리
     * 예약 취소로 인해 판매된 티켓 수량을 다시 판매 가능 재고로 복구
     * 
     * @param reservation 취소된 예약 정보
     */
    private void restoreTicketInventory(Reservation reservation) {
        Long reservationId = reservation.getId();
        Long ticketId = reservation.getTicket().getId();
        Integer reservedQuantity = reservation.getQuantity();
        
        log.debug("티켓 재고 복구 시작 - reservationId: {}, ticketId: {}, quantity: {}", 
                reservationId, ticketId, reservedQuantity);
        
        // 1. 티켓 엔티티 조회
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("티켓 정보 없음 - ticketId: {}", ticketId);
                    return new CustomException(CustomErrorCode.TICKET_NOT_EXIST);
                });
        
        // 2. 재고 복구 실행
        Integer beforeQuantity = ticket.getRemainingQuantity();
        ticket.restoreQuantity(reservedQuantity);
        Integer afterQuantity = ticket.getRemainingQuantity();
        
        log.debug("티켓 재고 복구 완료 - reservationId: {}, ticketId: {}, " +
                "복구 수량: {}, 복구 전: {}, 복구 후: {}", 
                reservationId, ticketId, reservedQuantity, beforeQuantity, afterQuantity);
    }
    
    /**
     * 박람회 원래 상태 확인
     * Refund 테이블의 isPartial 필드로 원래 PUBLISHED였는지 PENDING_PUBLISH였는지 확인
     * 
     * @param expoId 박람회 ID
     * @return true: 원래 PUBLISHED 상태, false: 원래 PENDING_PUBLISH 상태
     */
    private boolean determineOriginalExpoStatus(Long expoId) {
        // 1. 해당 박람회의 환불 신청 정보 조회
        Payment expoPayment = paymentRepository
                .findByTargetIdAndTargetType(expoId, PaymentTargetType.EXPO)
                .orElseThrow(() -> {
                    log.error("박람회 Payment 정보 없음 - expoId: {}", expoId);
                    return new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND);
                });
        
        // 2. 원래 환불 신청 정보 조회 (사용자가 처음 신청한 환불)
        // 박람회 주최자가 신청한 환불 정보만 조회
        List<Refund> userRefunds = refundRepository.findAll().stream()
                .filter(refund -> refund.getPayment().equals(expoPayment))
                .filter(refund -> !refund.getReason().contains("박람회 취소 승인으로 인한")) // 시스템 생성 환불 제외
                .sorted((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt())) // 생성일 오름차순
                .toList();
        
        if (userRefunds.isEmpty()) {
            log.error("박람회 사용자 환불 신청 정보 없음 - expoId: {}", expoId);
            throw new CustomException(CustomErrorCode.REFUND_NOT_FOUND);
        }
        
        // 사용자가 처음 신청한 환불의 isPartial 필드로 원래 상태 판단
        Refund originalRefund = userRefunds.get(0);
        boolean wasPublished = originalRefund.getIsPartial();
        log.debug("박람회 원래 상태 확인 - expoId: {}, 원본 환불: {}, isPartial: {}, 원래상태: {}", 
                expoId, originalRefund.getId(), wasPublished, wasPublished ? "PUBLISHED" : "PENDING_PUBLISH");
        
        return wasPublished;
    }
}