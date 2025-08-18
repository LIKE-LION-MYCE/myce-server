package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TicketSales {
    private String ticketType;
    private Long totalQuantity;
    private Long soldCount;
    private Long remainingCount;
    private BigDecimal unitPrice;
    private BigDecimal totalRevenue;
    
    @Builder
    public TicketSales(String ticketType, Long totalQuantity, Long soldCount, Long remainingCount, BigDecimal unitPrice, BigDecimal totalRevenue) {
        this.ticketType = ticketType;
        this.totalQuantity = totalQuantity;
        this.soldCount = soldCount;
        this.remainingCount = remainingCount;
        this.unitPrice = unitPrice;
        this.totalRevenue = totalRevenue;
    }
}