package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AdPaymentInfoResponse {
    private String title;
    private String requesterName;
    private LocalDate startAt;
    private LocalDate endAt;
    private Integer totalPrice;
    private Integer totalPayment;
    @Builder
    public AdPaymentInfoResponse(String title, String requesterName, LocalDate startAt, LocalDate endAt, Integer totalPrice, Integer totalPayment) {
        this.title = title;
        this.requesterName = requesterName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.totalPrice = totalPrice;
        this.totalPayment = totalPayment;
    }
}
