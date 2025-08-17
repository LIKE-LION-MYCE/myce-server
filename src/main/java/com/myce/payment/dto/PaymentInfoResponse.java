package com.myce.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PaymentInfoResponse {
    private Long id;
    private String title;
    private String type;
    private LocalDate serviceStartAt;
    private LocalDate serviceEndAt;
    private LocalDateTime createdAt;
    private Integer deposit;
    private BigDecimal ticketBenefit;
    private BigDecimal totalBenefit;
    private String status;
    @Builder
    public PaymentInfoResponse(Long id, String title, String type,
                               LocalDate serviceStartAt, LocalDate serviceEndAt,
                               LocalDateTime createdAt,
                               Integer deposit, BigDecimal ticketBenefit,
                               BigDecimal totalBenefit, String status) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.serviceStartAt = serviceStartAt;
        this.serviceEndAt = serviceEndAt;
        this.createdAt = createdAt;
        this.deposit = deposit;
        this.ticketBenefit = ticketBenefit;
        this.totalBenefit = totalBenefit;
        this.status = status;
    }
}
