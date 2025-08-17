package com.myce.settlement.service.impl;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.TicketRepository;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.repository.SettlementRepository;
import com.myce.settlement.service.SettlementSystemService;
import com.myce.settlement.service.mapper.SettlementSystemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Settlement 시스템/스케줄러 서비스 구현체
 * 자동 처리 및 시스템 관련 Settlement 작업을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementSystemServiceImpl implements SettlementSystemService {
    
    private final SettlementRepository settlementRepository;
    private final TicketRepository ticketRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    
    @Override
    @Transactional
    public void createInitialSettlement(Expo expo) {
        log.info("Settlement auto-creation started for expo: {}", expo.getId());
        
        // 1. Duplicate check
        if (settlementRepository.existsByExpoId(expo.getId())) {
            log.debug("Settlement already exists for expo: {}", expo.getId());
            return;
        }
        
        // 2. 실제 매출 계산 (티켓 판매 데이터 기반)
        Integer totalRevenue = calculateExpoRevenue(expo.getId());
        if (totalRevenue == null) totalRevenue = 0;
        
        // 3. 수수료 계산 (ExpoPaymentInfo에서 수수료율 가져오기)
        Integer commissionAmount = calculateCommissionAmount(expo.getId(), totalRevenue);
        
        // 4. Settlement creation using mapper
        Settlement settlement = SettlementSystemMapper.toInitialEntity(expo, totalRevenue, commissionAmount);
        settlementRepository.save(settlement);
        
        Integer netProfit = totalRevenue - commissionAmount;
        log.info("Settlement auto-created for expo {}: revenue={}, commission={}, netProfit={}", 
                 expo.getId(), totalRevenue, commissionAmount, netProfit);
    }
    
    /**
     * 박람회 실제 매출 계산 (티켓 판매 데이터 기반)
     * 
     * @param expoId 박람회 ID
     * @return 총 매출
     */
    private Integer calculateExpoRevenue(Long expoId) {
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);
        
        int totalRevenue = tickets.stream()
                .mapToInt(ticket -> {
                    // 판매된 수량 = 총 수량 - 남은 수량
                    int soldCount = ticket.getTotalQuantity() - ticket.getRemainingQuantity();
                    // 티켓별 총 판매금액 = 판매된 수량 * 티켓 가격
                    return soldCount * ticket.getPrice();
                })
                .sum();
        
        log.debug("Calculated expo revenue for expo {}: {} (from {} tickets)", 
                  expoId, totalRevenue, tickets.size());
        
        return totalRevenue;
    }
    
    /**
     * 수수료 계산 (ExpoPaymentInfo에서 수수료율 가져와서 계산)
     * 
     * @param expoId 박람회 ID
     * @param totalRevenue 총 매출
     * @return 수수료 금액
     */
    private Integer calculateCommissionAmount(Long expoId, Integer totalRevenue) {
        ExpoPaymentInfo paymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElse(null);
        
        if (paymentInfo == null || paymentInfo.getCommissionRate() == null) {
            log.warn("No payment info or commission rate found for expo {}, using default 5%", expoId);
            return (int)(totalRevenue * 0.05); // 5% 기본값
        }
        
        BigDecimal commissionRate = paymentInfo.getCommissionRate();
        int commissionAmount = totalRevenue * commissionRate.intValue() / 100;
        
        log.debug("Calculated commission for expo {}: {}% of {} = {}", 
                  expoId, commissionRate, totalRevenue, commissionAmount);
        
        return commissionAmount;
    }
}