package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 플랫폼 관리자용 박람회 취소 상세 정보 응답 DTO
 * 박람회 주최자 환불 정보 + 개별 예약자 환불 정보 포함
 */
@Getter
@Builder
public class ExpoCancelDetailResponse {
    
    // 박람회 기본 정보
    private String expoTitle;
    private String applicantName;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private String refundRequestDate;
    private String refundReason;
    
    // 박람회 주최자 환불 정보
    private Integer totalAmount;        // 총 결제 금액
    private Integer usedAmount;         // 사용한 금액  
    private Integer usedDays;           // 사용한 일수
    private Integer refundAmount;       // 주최자 환불 금액
    private Integer totalUsageFee;      // 총 이용료
    private Integer depositRefundAmount;  // 등록금 환불 금액
    private Integer usageFeeRefundAmount; // 이용료 환불 금액
    
    // 개별 예약자 환불 정보
    private Integer totalReservations;      // 총 예약 건수
    private Integer totalReservationAmount; // 개별 예약자 총 환불 금액
    private List<IndividualReservationRefund> reservationRefunds; // 개별 예약 상세
    
    @Getter
    @Builder
    public static class IndividualReservationRefund {
        private String reservationCode;    // 예약 코드
        private String reserverName;       // 예약자명
        private String ticketName;         // 티켓명
        private Integer quantity;          // 수량
        private Integer refundAmount;      // 개별 환불 금액
        private String userType;          // 회원/비회원
    }
}