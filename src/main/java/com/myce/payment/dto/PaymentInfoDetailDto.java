package com.myce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentInfoDetailDto {
    private String status;
    private int amount;
    private Long reservationId;

    public PaymentInfoDetailDto(String status, int amount) {
        this.status = status;
        this.amount = amount;
    }
}
