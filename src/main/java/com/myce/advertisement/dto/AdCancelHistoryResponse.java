package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AdCancelHistoryResponse {
    private String title;
    private String requesterName;
    private LocalDate startAt;
    private LocalDate endAt;
    private String paymentType;
    private String paymentCompanyName;
    private String paymentAccountInfo;
    private Integer totalAmount;
    @Builder
    public AdCancelHistoryResponse(String title, String requesterName, LocalDate startAt, LocalDate endAt, String paymentType, String paymentCompanyName, String paymentAccountInfo, Integer totalAmount) {
        this.title = title;
        this.requesterName = requesterName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.paymentType = paymentType;
        this.paymentCompanyName = paymentCompanyName;
        this.paymentAccountInfo = paymentAccountInfo;
        this.totalAmount = totalAmount;
    }
}
