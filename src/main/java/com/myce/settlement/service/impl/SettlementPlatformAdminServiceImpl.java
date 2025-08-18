package com.myce.settlement.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.notification.service.NotificationService;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.repository.SettlementRepository;
import com.myce.settlement.service.SettlementPlatformAdminService;
import com.myce.settlement.service.mapper.SettlementApprovalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Settlement 플랫폼 관리자 서비스 구현체
 * 플랫폼 관리자가 사용하는 Settlement 기능을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementPlatformAdminServiceImpl implements SettlementPlatformAdminService {
    
    private final SettlementRepository settlementRepository;
    private final ExpoRepository expoRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    
    @Override
    @Transactional
    public void approveSettlement(Long expoId, Long adminMemberId) {
        log.info("Settlement approval started for expo: {}, adminMemberId: {}", expoId, adminMemberId);
        
        // 1. Expo validation
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("Settlement approval failed - Expo not found: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 2. Status validation (must be SETTLEMENT_REQUESTED)
        if (expo.getStatus() != ExpoStatus.SETTLEMENT_REQUESTED) {
            log.error("Settlement approval failed - Invalid status: {}, expoId: {}", expo.getStatus(), expoId);
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
        
        // 3. Current admin member (use provided adminMemberId)
        Member adminMember = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> {
                    log.error("Settlement approval failed - Admin member not found: {}", adminMemberId);
                    return new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
                });
        
        // 4. Settlement entity lookup
        Settlement settlement = settlementRepository.findByExpoId(expoId)
                .orElseThrow(() -> {
                    log.error("Settlement approval failed - Settlement record not found: {}", expoId);
                    return new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND);
                });
        
        // 5. Update settlement with approval info using mapper
        Settlement approvedSettlement = SettlementApprovalMapper.toApprovedEntity(settlement, adminMember);
        settlementRepository.save(approvedSettlement);
        
        // 6. Update expo status (SETTLEMENT_REQUESTED → COMPLETED)
        String oldStatus = expo.getStatus().name();
        expo.approveSettlement();
        String newStatus = expo.getStatus().name();

        try {
            notificationService.sendExpoStatusChangeNotification(expoId, expo.getTitle(), oldStatus, newStatus);
        } catch (Exception e) {
            log.warn("정산 승인 알림 전송 실패 - expoId: {}, 오류: {}", expoId, e.getMessage());
        }


        log.info("Settlement approval completed - expoId: {}, adminMemberId: {}, settlementId: {}", 
                expoId, adminMember.getId(), settlement.getId());
    }
}