package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;

@Getter
@NoArgsConstructor
public class AdPaymentInfoRequest {
    private String title;
    private String requesterName;
    private LocalDate startAt;
    private LocalDate endAt;
    private HashMap<String, Integer> priceMap;
    private Integer totalPayment;
    @Builder
    public AdPaymentInfoRequest(String title, String requesterName,
                                LocalDate startAt, LocalDate endAt,
                                HashMap<String, Integer> priceMap,
                                Integer totalPayment) {
        this.title = title;
        this.requesterName = requesterName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.priceMap = priceMap;
        this.totalPayment = totalPayment;
    }
}
