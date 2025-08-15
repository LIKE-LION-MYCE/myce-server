package com.myce.settlement.service.impl;

import com.myce.expo.entity.Expo;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.repository.SettlementRepository;
import com.myce.settlement.service.SettlementSystemService;
import com.myce.settlement.service.mapper.SettlementSystemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Override
    @Transactional
    public void createInitialSettlement(Expo expo) {
        log.info("Settlement auto-creation started for expo: {}", expo.getId());
        
        // 1. Duplicate check
        if (settlementRepository.existsByExpoId(expo.getId())) {
            log.debug("Settlement already exists for expo: {}", expo.getId());
            return;
        }
        
        // 2. Revenue calculation (placeholder - 0 for now)
        Integer totalRevenue = calculateExpoRevenue(expo.getId());
        if (totalRevenue == null) totalRevenue = 0;
        
        // 3. Commission calculation (5% default)
        Integer commissionAmount = (int)(totalRevenue * 0.05);
        
        // 4. Settlement creation using mapper
        Settlement settlement = SettlementSystemMapper.toInitialEntity(expo, totalRevenue, commissionAmount);
        settlementRepository.save(settlement);
        
        Integer netProfit = totalRevenue - commissionAmount;
        log.info("Settlement auto-created for expo {}: revenue={}, commission={}, netProfit={}", 
                 expo.getId(), totalRevenue, commissionAmount, netProfit);
    }
    
    /**
     * Expo revenue calculation (reservation data based)
     * TODO: Implement actual reservation/payment data revenue calculation
     * 
     * @param expoId Expo ID
     * @return Total revenue
     */
    private Integer calculateExpoRevenue(Long expoId) {
        // Currently returns 0, add actual revenue calculation logic later
        // List<Reservation> reservations = reservationRepository.findByExpoId(expoId);
        // return reservations.stream()
        //     .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
        //     .mapToInt(r -> r.getTotalAmount())
        //     .sum();
        return 0;
    }
}