package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TicketSales {
    private String ticketType;
    private Long soldCount;
    private BigDecimal unitPrice;
    private BigDecimal totalRevenue;
    
    @Builder
    public TicketSales(String ticketType, Long soldCount, BigDecimal unitPrice, BigDecimal totalRevenue) {
        this.ticketType = ticketType;
        this.soldCount = soldCount;
        this.unitPrice = unitPrice;
        this.totalRevenue = totalRevenue;
    }
}