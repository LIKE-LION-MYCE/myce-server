package com.myce.settlement.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementRequest;
import com.myce.member.mapper.expo.ExpoSettlementReceiptMapper;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.repository.SettlementRepository;
import com.myce.settlement.service.SettlementExpoAdminService;
import com.myce.settlement.service.mapper.SettlementRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Settlement 박람회 관리자 서비스 구현체
 * 박람회 관리자가 사용하는 Settlement 기능을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementExpoAdminServiceImpl implements SettlementExpoAdminService {
    
    private final SettlementRepository settlementRepository;
    private final ExpoRepository expoRepository;
    private final TicketRepository ticketRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ReservationRepository reservationRepository;
    private final ExpoSettlementReceiptMapper expoSettlementReceiptMapper;
    
    @Override
    @Transactional
    public void requestSettlement(Long expoId, ExpoSettlementRequest request) {
        log.info("Settlement request started for expo: {}", expoId);
        
        // 1. Expo validation
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("Settlement request failed - Expo not found: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 2. Status validation (must be PUBLISH_ENDED)
        if (expo.getStatus() != ExpoStatus.PUBLISH_ENDED) {
            log.error("Settlement request failed - Invalid expo status: {}, expoId: {}", expo.getStatus(), expoId);
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
        
        // 3. Check if settlement already exists and update or create
        Settlement existingSettlement = settlementRepository.findByExpoId(expoId).orElse(null);
        
        if (existingSettlement != null) {
            // Update existing settlement with bank information
            existingSettlement.updateBankInfo(
                request.getReceiverName(),
                request.getBankName(), 
                request.getBankAccount()
            );
            settlementRepository.save(existingSettlement);
            log.info("Settlement updated with bank info - expoId: {}", expoId);
        } else {
            // Create new settlement if not exists (fallback)
            // 5. Get settlement receipt data
            ExpoSettlementReceiptResponse settlementReceipt = getSettlementReceiptData(expoId);
            
            // 6. Create settlement entity using mapper
            Settlement settlement = SettlementRequestMapper.toRequestEntity(expo, request, settlementReceipt);
            settlementRepository.save(settlement);
            log.info("New settlement created - expoId: {}, netProfit: {}", 
                    expoId, settlementReceipt.getNetProfit());
        }
        
        // 4. Update expo status to SETTLEMENT_REQUESTED
        expo.updateStatus(ExpoStatus.SETTLEMENT_REQUESTED);
        
        log.info("Settlement request completed - expoId: {}", expoId);
    }
    
    /**
     * Settlement receipt data generation
     * Integrates ticket and payment info for settlement calculation
     * 
     * @param expoId Expo ID
     * @return Settlement receipt response
     */
    private ExpoSettlementReceiptResponse getSettlementReceiptData(Long expoId) {
        // Expo lookup
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        // Ticket list lookup
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);
        
        // CONFIRMED reservations lookup for settlement calculation
        List<Reservation> confirmedReservations = reservationRepository.findByExpoId(expoId).stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED)
                .toList();
        
        // Payment info lookup (for commission rate)
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        // Settlement info lookup for completion data (currently null)
        Settlement settlement = null;
        if (expo.getStatus() == ExpoStatus.COMPLETED) {
            settlement = settlementRepository.findByExpoId(expoId).orElse(null);
        }
        
        // Mapper processing for all info
        return expoSettlementReceiptMapper.toSettlementReceiptResponse(expo, tickets, confirmedReservations, expoPaymentInfo, settlement);
    }
}