package com.myce.refund.service.impl;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.common.exception.CustomException;
import com.myce.common.exception.CustomErrorCode;
import com.myce.member.dto.MileageUpdateRequest;
import com.myce.notification.service.NotificationService;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.refund.PaymentRefundService;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.refund.dto.RefundRequestDto;
import com.myce.refund.dto.ReservationRefundCalculation;
import com.myce.refund.service.RefundRequestService;
import com.myce.refund.service.ReservationRefundCalculationService;
import com.myce.member.service.MemberMileageService;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundRequestServiceImpl implements RefundRequestService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRefundService paymentRefundService;
    private final ReservationRefundCalculationService refundCalculationService;
    private final MemberMileageService memberMileageService;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;


    @Override
    public void createRefundRequest(Long memberId, Long expoId, RefundRequestDto requestDto) {
        // 1. expoId로 결제 정보 조회
        Payment payment = paymentRepository.findByTargetIdAndTargetType(expoId, PaymentTargetType.EXPO)
                .orElseThrow(() -> new IllegalArgumentException("해당 엑스포의 결제 정보를 찾을 수 없습니다."));

        // 2. 이미 환불 신청이 있는지 확인
        if (refundRepository.existsByPaymentAndStatus(payment, RefundStatus.PENDING)) {
            throw new IllegalStateException("이미 환불 신청이 진행 중입니다.");
        }

        // 3. 엑스포 정보 조회 및 상태 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 엑스포입니다."));
        
        // 4. 환불 가능 상태 확인 (PUBLISHED 상태는 7일 규칙 포함)
        validateRefundEligibility(expo.getStatus(), expo);
        
        // 5. 현재 엑스포 상태에 따른 환불 타입 결정
        boolean isPartialRefund = determineRefundType(expo.getStatus());
        System.out.println("[환불 신청] 엑스포 ID: " + expoId + ", 현재 상태: " + expo.getStatus() + ", 부분환불 여부: " + isPartialRefund);

        try {
            String oldStatus = expo.getStatus().name();
            notificationService.sendExpoStatusChangeNotification(expoId, expo.getTitle(), oldStatus, "PENDING_CANCEL");
        } catch (Exception e) {
            log.warn("박람회 환불 신청 알림 전송 실패 - expoId: {}, 오류: {}", expoId, e.getMessage());
        }

        // 6. 엑스포 상태를 PENDING_CANCEL로 변경
        expo.updateStatus(ExpoStatus.PENDING_CANCEL);
        
        // 7. 환불 엔티티 생성 및 저장
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(requestDto.getAmount())
                .reason(requestDto.getReason())
                .status(RefundStatus.PENDING)
                .isPartial(isPartialRefund)
                .build();

        refundRepository.save(refund);


    }
    
    @Override
    public void createReservationRefund(Long memberId, Long reservationId, String reason) {

        // 1. 예매 정보 조회 및 검증
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        ReservationPaymentInfo reservationInfo = reservationPaymentInfoRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        
        // 2. 예매 소유자 확인
        if (!reservation.getUserId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND);
        }
        
        // 3. 예매 상태 확인
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(CustomErrorCode.RESERVATION_STATUS_INVALID);
        }
        
        // 4. 결제 정보 조회
        Payment payment = paymentRepository.findByTargetIdAndTargetType(
                reservationId, PaymentTargetType.RESERVATION)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        // 5. 이미 환불된 결제인지 확인
        if (refundRepository.existsByPaymentAndStatus(payment, RefundStatus.REFUNDED)) {
            throw new CustomException(CustomErrorCode.ALREADY_REFUNDED);
        }
        
        // 6. 환불 금액 계산 (수수료, 마일리지 등)
        ReservationRefundCalculation calculation = refundCalculationService.calculateRefundAmount(reservationId);
        
        // 7. 환불 불가 조건 확인 (실제 환불 금액이 0원 이하)
        if (calculation.getActualRefundAmount() <= 0) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }
        
        // 8. 환불 처리 (즉시 실행)
        PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
                .impUid(payment.getImpUid())
                .cancelAmount(calculation.getActualRefundAmount())
                .reason(reason)
                .build();
                
        paymentRefundService.refundPayment(refundRequest);
        
        // 9. 마일리지 처리 (복원/차감)
        MileageUpdateRequest mileageRequest = new MileageUpdateRequest(
            reservationInfo.getUsedMileage(),   // usedMileage (복원할 마일리지)
            reservationInfo.getSavedMileage()     // savedMileage (차감할 마일리지)
        );

        memberMileageService.revertMileageForReservationRefund(memberId, mileageRequest);
        
        // 10. 티켓 수량 복원
        Ticket ticket = reservation.getTicket();
        ticket.restoreQuantity(reservation.getQuantity());
        ticketRepository.save(ticket);
        
        // 11. 예매 상태 변경
        reservation.updateStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

    }
    
    /**
     * 환불 가능 상태인지 확인
     * PUBLISHED 상태의 경우 7일 규칙도 함께 검증
     * 
     * @param expoStatus 현재 엑스포 상태
     * @param expo 엑스포 엔티티 (7일 규칙 확인용)
     * @throws IllegalStateException 환불 불가능한 상태인 경우
     */
    private void validateRefundEligibility(ExpoStatus expoStatus, Expo expo) {
        switch (expoStatus) {
            case PUBLISHED:
                // 게시중일 때만 7일 규칙 적용
                validateSevenDayRule(expo);
                break;
            case PENDING_PUBLISH:
                // 게시 대기는 7일 규칙 적용 안함 (언제든 취소 가능)
                break;
            case PUBLISH_ENDED:
            case COMPLETED:
            case CANCELLED:
            case REJECTED:
                throw new IllegalStateException("현재 상태에서는 환불 신청이 불가능합니다.");
            case PENDING_CANCEL:
                throw new IllegalStateException("이미 환불 신청이 진행 중입니다.");
            default:
                // 기타 환불 가능한 상태
                break;
        }
    }
    
    /**
     * 7일 규칙 검증 (게시중 박람회만 적용)
     * 개최일 7일 전부터는 환불 불가
     * 
     * @param expo 엑스포 엔티티
     * @throws CustomException 7일 이내인 경우
     */
    private void validateSevenDayRule(Expo expo) {
        LocalDate today = LocalDate.now();
        long daysUntilStart = ChronoUnit.DAYS.between(today, expo.getStartDate());
        
        if (daysUntilStart < 7) {
            throw new CustomException(CustomErrorCode.REFUND_SEVEN_DAY_RULE_VIOLATION);
        }
    }
    
    /**
     * 엑스포 상태에 따른 환불 타입 결정
     * @param expoStatus 현재 엑스포 상태
     * @return true: 부분 환불, false: 전액 환불
     */
    private boolean determineRefundType(ExpoStatus expoStatus) {
        switch (expoStatus) {
            case PENDING_PUBLISH:
                // 게시 대기 중: 전액 환불 (등록금 + 이용료)
                return false;
            case PUBLISHED:
                // 게시 중: 부분 환불 (사용한 일수 제외한 이용료만)
                return true;
            default:
                // 기타 상태는 부분 환불로 처리
                return true;
        }
    }
}