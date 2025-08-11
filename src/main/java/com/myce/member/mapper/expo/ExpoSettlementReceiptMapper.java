package com.myce.member.mapper.expo;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.payment.entity.ExpoPaymentInfo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ExpoSettlementReceiptMapper {

    public ExpoSettlementReceiptResponse toSettlementReceiptResponse(Expo expo,
            List<Ticket> tickets,
            ExpoPaymentInfo expoPaymentInfo) {

        // 티켓별 판매 정보 계산
        List<ExpoSettlementReceiptResponse.TicketSalesInfo> ticketSales = tickets.stream()
                .map(this::buildTicketSalesInfo)
                .toList();

        // 총 매출 계산
        int totalRevenue = ticketSales.stream()
                .mapToInt(ExpoSettlementReceiptResponse.TicketSalesInfo::getTotalSales)
                .sum();

        // 결제 시점에 저장된 수수료율 사용
        BigDecimal commissionRate = expoPaymentInfo.getCommissionRate();
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