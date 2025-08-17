package com.myce.refund.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.refund.dto.ReservationRefundCalculation;
import com.myce.refund.service.ReservationRefundCalculationService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.system.entity.RefundFeeSetting;
import com.myce.system.entity.type.StandardType;
import com.myce.system.repository.RefundFeeSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationRefundCalculationServiceImpl implements ReservationRefundCalculationService {

    private final ReservationRepository reservationRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final RefundFeeSettingRepository refundFeeSettingRepository;

    @Override
    public ReservationRefundCalculation calculateRefundAmount(Long reservationId) {
        // 1. 예매 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        // 2. 결제 정보 조회
        ReservationPaymentInfo paymentInfo = reservationPaymentInfoRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        // 3. 박람회 시작일까지 남은 일수 계산
        long daysUntilExpo = ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getExpo().getStartDate().atStartOfDay());

        // 4. 적용 가능한 환불 수수료 설정 조회 (BEFORE_EXPO_START만 사용)
        RefundFeeSetting feeSetting = getApplicableRefundFeeSetting(daysUntilExpo);

        // 5. 환불 수수료 계산
        Integer refundFee = calculateRefundFee(paymentInfo.getTotalAmount(), feeSetting);

        // 6. 실제 환불 금액 계산
        Integer actualRefundAmount = paymentInfo.getTotalAmount() - refundFee;

        return ReservationRefundCalculation.builder()
                .originalAmount(paymentInfo.getTotalAmount())
                .refundFee(refundFee)
                .actualRefundAmount(actualRefundAmount)
                .restoreMileage(paymentInfo.getUsedMileage())
                .deductMileage(paymentInfo.getSavedMileage())
                .feeDescription(String.format("%s (%.1f%% 수수료)", 
                    feeSetting.getName(), feeSetting.getFeeRate()))
                .build();
    }

    private RefundFeeSetting getApplicableRefundFeeSetting(long daysUntilExpo) {
        // 박람회 시작일 기준으로만 환불 수수료 적용
        return refundFeeSettingRepository.findAll().stream()
                .filter(RefundFeeSetting::isActive)
                .filter(setting -> setting.getStandardType() == StandardType.BEFORE_EXPO_START)
                .filter(setting -> daysUntilExpo <= setting.getStandardDayCount())
                .min((s1, s2) -> Integer.compare(s1.getStandardDayCount(), s2.getStandardDayCount()))
                .orElse(getDefaultRefundFeeSetting());
    }

    private RefundFeeSetting getDefaultRefundFeeSetting() {
        // 기본 환불 수수료 설정 (환불 불가 - 100% 수수료)
        return RefundFeeSetting.builder()
                .standardType(StandardType.BEFORE_EXPO_START)
                .standardDayCount(0)
                .feeRate(BigDecimal.valueOf(100))
                .description("환불 불가 기간")
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(1))
                .build();
    }

    private Integer calculateRefundFee(Integer totalAmount, RefundFeeSetting feeSetting) {
        BigDecimal amount = BigDecimal.valueOf(totalAmount);
        BigDecimal fee = amount.multiply(feeSetting.getFeeRate().divide(BigDecimal.valueOf(100)));
        return fee.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}