package com.myce.expo.service.mapper;

import com.myce.common.entity.BusinessProfile;
import com.myce.expo.dto.ExpoCancelDetailResponse;
import com.myce.expo.entity.Expo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Refund;

import java.util.List;

/**
 * 박람회 취소 상세 정보 매퍼
 */
public class ExpoCancelDetailMapper {

    /**
     * 박람회 취소 상세 응답 DTO 생성
     */
    public static ExpoCancelDetailResponse toResponse(
            Expo expo,
            BusinessProfile businessProfile,
            ExpoPaymentInfo expoPaymentInfo,
            Refund expoRefund,
            String refundRequestDate,
            Integer usedAmount,
            Integer usedDays,
            Integer totalReservations,
            Integer totalReservationAmount,
            List<ExpoCancelDetailResponse.IndividualReservationRefund> reservationRefunds
    ) {
        // 등록금/이용료 환불 금액 계산
        Integer depositAmount = expo.getIsPremium() ? 
            expoPaymentInfo.getPremiumDeposit() : 
            expoPaymentInfo.getDeposit();
        
        Integer depositRefundAmount;
        Integer usageFeeRefundAmount;
        
        if (expoRefund.getIsPartial()) {
            // 부분환불 (게시중, 기타): 등록금 환불 없음, 이용료만 환불
            depositRefundAmount = 0;
            usageFeeRefundAmount = expoRefund.getAmount();
        } else {
            // 전액환불 (게시대기): 등록금 + 이용료 환불
            depositRefundAmount = depositAmount;
            usageFeeRefundAmount = expoRefund.getAmount() - depositAmount;
        }

        return ExpoCancelDetailResponse.builder()
                .expoTitle(expo.getTitle())
                .applicantName(businessProfile != null ? businessProfile.getCompanyName() : 
                              expo.getMember().getName())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .refundRequestDate(refundRequestDate)
                .refundReason(expoRefund.getReason())
                .totalAmount(expoPaymentInfo.getTotalAmount())
                .usedAmount(usedAmount)
                .usedDays(usedDays)
                .refundAmount(expoRefund.getAmount())
                .totalUsageFee(expoPaymentInfo.getDailyUsageFee() * expoPaymentInfo.getTotalDay())
                .depositRefundAmount(depositRefundAmount)
                .usageFeeRefundAmount(usageFeeRefundAmount)
                .totalReservations(totalReservations)
                .totalReservationAmount(totalReservationAmount)
                .reservationRefunds(reservationRefunds)
                .build();
    }
}