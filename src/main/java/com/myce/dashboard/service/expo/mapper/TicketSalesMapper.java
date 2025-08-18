package com.myce.dashboard.service.expo.mapper;

import com.myce.dashboard.dto.expo.TicketSales;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class TicketSalesMapper {

    /**
     * DB 쿼리 결과(Object[])를 TicketSales 리스트로 변환
     * @param queryResults DB에서 조회한 [티켓타입, 판매수, 평균가격, 총매출] 결과
     * @return 티켓 판매 상세 리스트
     */
    public List<TicketSales> mapFromQueryResults(List<Object[]> queryResults) {
        List<TicketSales> ticketSalesDetail = new ArrayList<>();
        
        for (Object[] result : queryResults) {
            String ticketType = (String) result[0];
            Long totalQuantity = ((Number) result[1]).longValue();
            Long soldCount = ((Number) result[2]).longValue();
            Long remainingCount = ((Number) result[3]).longValue();
            BigDecimal unitPrice = result[4] != null ? 
                new BigDecimal(result[4].toString()).setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal totalRevenue = result[5] != null ? 
                new BigDecimal(result[5].toString()) : BigDecimal.ZERO;
            
            ticketSalesDetail.add(TicketSales.builder()
                    .ticketType(ticketType)
                    .totalQuantity(totalQuantity)
                    .soldCount(soldCount)
                    .remainingCount(remainingCount)
                    .unitPrice(unitPrice)
                    .totalRevenue(totalRevenue)
                    .build());
        }
        
        return ticketSalesDetail;
    }
}