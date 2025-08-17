package com.myce.member.mapper.expo;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.reservation.entity.Reservation;
import com.myce.settlement.entity.Settlement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ExpoSettlementReceiptMapper {

    public ExpoSettlementReceiptResponse toSettlementReceiptResponse(Expo expo,
            List<Ticket> tickets,
            List<Reservation> confirmedReservations,
            ExpoPaymentInfo expoPaymentInfo,
            Settlement settlement) {

        // 티켓별 예약 집계 (CONFIRMED 상태만)
        Map<Long, Integer> ticketSalesMap = confirmedReservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getTicket().getId(),
                        Collectors.summingInt(Reservation::getQuantity)
                ));

        // 티켓별 판매 정보 계산 (reservation 테이블 기반)
        List<ExpoSettlementReceiptResponse.TicketSalesInfo> ticketSales = tickets.stream()
                .map(ticket -> buildTicketSalesInfoFromReservations(ticket, ticketSalesMap))
                .toList();

        // Settlement 테이블에서 금액 정보 우선 사용
        int totalRevenue, commissionAmount, netProfit;
        BigDecimal commissionRate;
        
        if (settlement != null && settlement.getTotalAmount() != null) {
            // Settlement 테이블에서 정확한 값 사용
            totalRevenue = settlement.getTotalAmount();
            commissionAmount = settlement.getSupplyAmount() != null ? settlement.getSupplyAmount() : 0;
            netProfit = settlement.getSettleAmount() != null ? settlement.getSettleAmount() : 0;
            
            // 수수료율은 ExpoPaymentInfo에서 가져오기 (백분율 표시용)
            commissionRate = expoPaymentInfo.getCommissionRate();
        } else {
            // Settlement이 없으면 기존 로직 (실시간 계산)
            totalRevenue = ticketSales.stream()
                    .mapToInt(ExpoSettlementReceiptResponse.TicketSalesInfo::getTotalSales)
                    .sum();
            
            commissionRate = expoPaymentInfo.getCommissionRate();
            commissionAmount = totalRevenue * commissionRate.intValue() / 100;
            netProfit = totalRevenue - commissionAmount;
        }

        // Builder로 기본 정보 설정
        ExpoSettlementReceiptResponse.ExpoSettlementReceiptResponseBuilder builder = ExpoSettlementReceiptResponse.builder()
                .expoTitle(expo.getTitle())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .status(expo.getStatus())
                .ticketSales(ticketSales)
                .totalRevenue(totalRevenue)
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmount)
                .netProfit(netProfit)
                .issueDate(LocalDate.now());
        
        // Settlement 정보가 있으면 정산 완료 정보 추가
        if (settlement != null) {
            builder.receiverName(settlement.getReceiverName())
                   .bankName(settlement.getBankName())
                   .bankAccount(settlement.getBankAccount())
                   .settlementAt(settlement.getSettlementAt() != null ? 
                                settlement.getSettlementAt().toString() : null);
            
            // 정산 처리 담당자 정보
            if (settlement.getAdminMember() != null) {
                builder.adminName(settlement.getAdminMember().getName());
            }
        }
        
        return builder.build();
    }

    /**
     * 기존 메서드 (remaining_quantity 기반) - deprecated
     */
    @Deprecated
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
    
    /**
     * 예약 테이블 기반 티켓 판매 정보 생성 (CONFIRMED 상태만 집계)
     * 
     * @param ticket 티켓 정보
     * @param ticketSalesMap 티켓별 판매 수량 맵 (ticketId -> 판매수량)
     * @return 티켓 판매 정보 DTO
     */
    private ExpoSettlementReceiptResponse.TicketSalesInfo buildTicketSalesInfoFromReservations(
            Ticket ticket, 
            Map<Long, Integer> ticketSalesMap) {
        
        // reservation 테이블에서 집계한 실제 판매 수량
        int soldCount = ticketSalesMap.getOrDefault(ticket.getId(), 0);
        
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