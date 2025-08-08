package com.myce.member.mapper;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.member.dto.ExpoSettlementReceiptResponse;
import com.myce.system.entity.ExpoFeeSetting;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class ExpoSettlementReceiptMapper {
    
    public ExpoSettlementReceiptResponse toSettlementReceiptResponse(Expo expo, 
                                                                    List<Ticket> tickets, 
                                                                    ExpoFeeSetting feeSetting) {
        
        // 티켓별 판매 정보 계산
        List<ExpoSettlementReceiptResponse.TicketSalesInfo> ticketSales = tickets.stream()
                .map(this::buildTicketSalesInfo)
                .toList();
        
        // 총 매출 계산
        int totalRevenue = ticketSales.stream()
                .mapToInt(ExpoSettlementReceiptResponse.TicketSalesInfo::getTotalSales)
                .sum();
        
        // 수수료 계산
        BigDecimal commissionRate = feeSetting.getSettlementCommission();
        int commissionAmount = totalRevenue * commissionRate.intValue() / 100;
        
        // 순수익 계산
        int netProfit = totalRevenue - commissionAmount;
        
        return ExpoSettlementReceiptResponse.builder()
                .expoTitle(expo.getTitle())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .status(expo.getStatus())
                .ticketSales(ticketSales)
                .totalRevenue(totalRevenue)
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmount)
                .netProfit(netProfit)
                .issueDate(LocalDate.now())
                .build();
    }
    
    private ExpoSettlementReceiptResponse.TicketSalesInfo buildTicketSalesInfo(Ticket ticket) {
        // 판매된 수량 = 총 수량 - 남은 수량
        int soldCount = ticket.getTotalQuantity() - ticket.getRemainingQuantity();
        // 티켓별 총 판매금액 = 판매된 수량 * 티켓 가격
        int totalSales = soldCount * ticket.getPrice();
        
        return ExpoSettlementReceiptResponse.TicketSalesInfo.builder()
                .ticketId(ticket.getId())
                .ticketName(ticket.getName())
                .ticketPrice(ticket.getPrice())
                .soldCount(soldCount)
                .totalSales(totalSales)
                .build();
    }
}