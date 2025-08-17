package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class PaymentStats {
    // Redis 캐시 데이터
    private Long pendingPayments;
    private BigDecimal todayRevenue;
    
    // RDB 직접 조회 데이터
    private Long completedPayments;
    private Long canceledPayments;
    private Long refundedPayments;
    private BigDecimal totalRevenue;
    private List<TicketSales> ticketSalesDetail;
    private String dataSource;
    
    @Builder
    public PaymentStats(Long pendingPayments,
                       BigDecimal todayRevenue,
                       Long completedPayments,
                       Long canceledPayments,
                       Long refundedPayments,
                       BigDecimal totalRevenue,
                       List<TicketSales> ticketSalesDetail,
                       String dataSource) {
        this.pendingPayments = pendingPayments;
        this.todayRevenue = todayRevenue;
        this.completedPayments = completedPayments;
        this.canceledPayments = canceledPayments;
        this.refundedPayments = refundedPayments;
        this.totalRevenue = totalRevenue;
        this.ticketSalesDetail = ticketSalesDetail;
        this.dataSource = dataSource != null ? dataSource : "mixed";
    }
}