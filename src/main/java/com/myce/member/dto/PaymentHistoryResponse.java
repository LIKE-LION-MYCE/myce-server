package com.myce.member.dto;

import com.myce.payment.entity.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {
    
    private String paymentNumber;    // 결제번호
    private LocalDateTime paymentDate;    // 날짜
    private String expoTitle;        // 박람회이름
    private Integer totalAmount;     // 결제금액
    private PaymentStatus status;    // 상태
    private String reservationCode;  // 예매 상세보기용
}