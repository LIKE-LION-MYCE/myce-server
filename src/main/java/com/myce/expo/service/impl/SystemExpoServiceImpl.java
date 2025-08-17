package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.SystemExpoService;
import com.myce.settlement.service.SettlementSystemService;
import com.myce.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class SystemExpoServiceImpl implements SystemExpoService {
    
    private final ExpoRepository expoRepository;
    private final SettlementSystemService settlementSystemService;
    private final NotificationService notificationService;
    
    private static final List<ExpoStatus> ACTIVE_STATUSES = List.of(
            ExpoStatus.PUBLISHED,
            ExpoStatus.PENDING_CANCEL
    );
    
    @Override
    public void checkAvailablePeriod(Long expoId, LocalDate startedAt, LocalDate endedAt) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 박람회 ID: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
                
        
        // 박람회 기간 검증 로직 (필요시 확장)
        if (startedAt.isAfter(endedAt)) {
            log.error("잘못된 박람회 기간 - 시작일이 종료일보다 늦음: {} > {}", startedAt, endedAt);
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public int publishPendingExpos() {
        
        List<Expo> pendingExpos = expoRepository
                .findAllByDisplayStartDateLessThanEqualAndStatus(
                        LocalDate.now(),
                        ExpoStatus.PENDING_PUBLISH);
        
        
        for (Expo expo : pendingExpos) {
            String oldStatus = expo.getStatus().name();
            expo.publish();
            String newStatus = expo.getStatus().name();
            
            // 상태 변경 알림 전송
            try {
                notificationService.sendExpoStatusChangeNotification(expo.getId(), expo.getTitle(), oldStatus, newStatus);
            } catch (Exception e) {
                log.warn("박람회 자동 게시 알림 전송 실패 - expoId: {}, 오류: {}", expo.getId(), e.getMessage());
            }
        }
        
        if (!pendingExpos.isEmpty()) {
            expoRepository.saveAll(pendingExpos);
            log.info("박람회 자동 게시 완료 - 처리된 박람회 수: {}", pendingExpos.size());
        }
        
        return pendingExpos.size();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public int closeCompletedExpos() {
        
        List<Expo> endedExpos = expoRepository
                .findAllByDisplayEndDateLessThanAndStatus(
                        LocalDate.now(),
                        ExpoStatus.PUBLISHED);
        
        
        for (Expo expo : endedExpos) {
            String oldStatus = expo.getStatus().name();
            expo.complete(); // PUBLISHED → PUBLISH_ENDED
            String newStatus = expo.getStatus().name();
            
            // 상태 변경 알림 전송
            try {
                notificationService.sendExpoStatusChangeNotification(expo.getId(), expo.getTitle(), oldStatus, newStatus);
            } catch (Exception e) {
                log.warn("박람회 게시 종료 알림 전송 실패 - expoId: {}, 오류: {}", expo.getId(), e.getMessage());
            }
            
            // Settlement 자동 생성 (SettlementSystemService로 위임)
            settlementSystemService.createInitialSettlement(expo);
        }
        
        if (!endedExpos.isEmpty()) {
            expoRepository.saveAll(endedExpos);
            log.info("박람회 게시 종료 완료 - 처리된 박람회 수: {}", endedExpos.size());
        }
        
        return endedExpos.size();
    }

    @Override
    public void refreshExpoCache() {
        
        // Redis 캐시 갱신 로직이 필요한 경우 구현
        // 현재는 박람회에서 Redis 캐시를 사용하지 않으므로 빈 구현
        
    }
    
}