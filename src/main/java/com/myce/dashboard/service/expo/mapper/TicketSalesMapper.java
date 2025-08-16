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
            Long soldCount = ((Number) result[1]).longValue();
            BigDecimal avgPrice = result[2] != null ? 
                new BigDecimal(result[2].toString()).setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal totalRevenue = result[3] != null ? 
                new BigDecimal(result[3].toString()) : BigDecimal.ZERO;
            
            ticketSalesDetail.add(TicketSales.builder()
                    .ticketType(ticketType)
                    .soldCount(soldCount)
                    .unitPrice(avgPrice)
                    .totalRevenue(totalRevenue)
                    .build());
        }
        
        return ticketSalesDetail;
    }
}