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
    private Integer totalDays;
    private Integer totalPrice;
    private BigDecimal ticketFee;
    private BigDecimal totalRevenue;
    private String status;
    @Builder
    public PaymentInfoResponse(Long id, String title, String type,
                               LocalDate serviceStartAt, LocalDate serviceEndAt,
                               LocalDateTime createdAt,
                               Integer totalDays, Integer totalPrice, BigDecimal ticketFee,
                               BigDecimal totalRevenue, String status) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.serviceStartAt = serviceStartAt;
        this.serviceEndAt = serviceEndAt;
        this.createdAt = createdAt;
        this.totalDays = totalDays;
        this.totalPrice = totalPrice;
        this.ticketFee = ticketFee;
        this.totalRevenue = totalRevenue;
        this.status = status;
    }
}
