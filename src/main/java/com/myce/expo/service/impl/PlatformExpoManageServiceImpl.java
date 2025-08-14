package com.myce.expo.service.impl;

import com.myce.common.entity.RejectInfo;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.RejectInfoRepository;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.PlatformExpoManageService;
import com.myce.expo.service.mapper.ExpoPaymentInfoMapper;
import com.myce.expo.service.mapper.RejectInfoMapper;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.system.entity.ExpoFeeSetting;
import com.myce.system.repository.ExpoFeeSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

/**
 * 플랫폼 관리자용 박람회 신청 관리 서비스 구현체
 * 박람회 승인/거절 처리를 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlatformExpoManageServiceImpl implements PlatformExpoManageService {

    // 의존성 주입
    private final ExpoRepository expoRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final RejectInfoRepository rejectInfoRepository;
    private final ExpoFeeSettingRepository expoFeeSettingRepository;

    /**
     * 박람회 신청 승인 처리
     * - 박람회 상태를 PENDING_APPROVAL -> PENDING_PAYMENT로 변경
     * - ExpoPaymentInfo 생성하여 결제 대기 상태로 설정
     * 
     * @param expoId 승인할 박람회 ID
     */
    @Override
    public void approveExpoApplication(Long expoId) {
        // 1. 요청 정보 로깅
        
        // 2. 박람회 엔티티 조회 및 검증
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 박람회 ID로 승인 시도: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 3. 수수료 설정 조회
        ExpoFeeSetting feeSetting = expoFeeSettingRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.error("박람회 수수료 설정이 존재하지 않음");
                    return new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND);
                });
        
        // 4. 박람회 일수 및 총 금액 계산
        int totalDays = calculateTotalDays(expo);
        Integer totalAmount = calculateTotalAmount(expo, feeSetting, totalDays);
        
        
        // 5. ExpoPaymentInfo 생성 (Mapper 사용)
        ExpoPaymentInfo paymentInfo = ExpoPaymentInfoMapper.toEntity(expo, feeSetting, totalDays, totalAmount);
        
        expoPaymentInfoRepository.save(paymentInfo);
        
        // 6. 박람회 상태 변경 (Entity 메서드 사용)
        expo.approve();
        
        log.info("박람회 신청 승인 완료 - expoId: {}, totalAmount: {}", expoId, totalAmount);
    }

    /**
     * 박람회 신청 거절 처리
     * - 박람회 상태를 PENDING_APPROVAL -> REJECTED로 변경
     * - RejectInfo 생성하여 거절 사유 저장
     * 
     * @param expoId 거절할 박람회 ID
     * @param reason 거절 사유
     */
    @Override
    public void rejectExpoApplication(Long expoId, String reason) {
        // 1. 요청 정보 로깅
        
        // 2. 박람회 엔티티 조회 및 검증
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 박람회 ID로 거절 시도: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 3. 거절 정보 생성 (Mapper 사용)
        RejectInfo rejectInfo = RejectInfoMapper.toEntity(expo.getId(), reason);
        
        rejectInfoRepository.save(rejectInfo);
        
        // 4. 박람회 상태 변경 (Entity 메서드 사용)
        expo.reject();
        
        log.info("박람회 신청 거절 완료 - expoId: {}", expoId);
    }

    /**
     * 박람회 취소 승인 처리
     * - 박람회 상태를 PENDING_CANCEL -> CANCELLED로 변경
     * - 환불 처리 로직 포함
     * 
     * @param expoId 취소 승인할 박람회 ID
     */
    @Override
    @Transactional
    public void approveCancellation(Long expoId) {
        // 1. 요청 정보 로깅
        log.info("박람회 취소 승인 요청 - expoId: {}", expoId);
        
        // 2. 박람회 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("박람회 취소 승인 실패 - 존재하지 않는 박람회 ID: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 3. 상태 검증 (PENDING_CANCEL 상태만 취소 승인 가능)
        if (expo.getStatus() != ExpoStatus.PENDING_CANCEL) {
            log.error("박람회 취소 승인 실패 - 잘못된 상태: {}, expoId: {}", expo.getStatus(), expoId);
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
        
        // 4. 박람회 상태 변경 (PENDING_CANCEL -> CANCELLED)
        expo.approveCancellation();  // Entity 메서드 사용
        
        // 5. 결제 정보 상태 업데이트 (환불 처리)
        ExpoPaymentInfo paymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElse(null);
        
        if (paymentInfo != null) {
            paymentInfo.setStatus(PaymentStatus.REFUNDED);  // 환불 상태로 변경
        }
        
        log.info("박람회 취소 승인 완료 - expoId: {}", expoId);
    }

    /**
     * 박람회 정산 승인 처리
     * - 박람회 상태를 SETTLEMENT_REQUESTED -> COMPLETED로 변경
     * 
     * @param expoId 정산 승인할 박람회 ID
     */
    @Override
    @Transactional
    public void approveSettlement(Long expoId) {
        // 1. 요청 정보 로깅
        log.info("박람회 정산 승인 요청 - expoId: {}", expoId);
        
        // 2. 박람회 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> {
                    log.error("박람회 정산 승인 실패 - 존재하지 않는 박람회 ID: {}", expoId);
                    return new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
                });
        
        // 3. 상태 검증 (SETTLEMENT_REQUESTED 상태만 정산 승인 가능)
        if (expo.getStatus() != ExpoStatus.SETTLEMENT_REQUESTED) {
            log.error("박람회 정산 승인 실패 - 잘못된 상태: {}, expoId: {}", expo.getStatus(), expoId);
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
        
        // 4. 박람회 상태 변경 (SETTLEMENT_REQUESTED -> COMPLETED)
        expo.approveSettlement();  // Entity 메서드 사용
        
        log.info("박람회 정산 승인 완료 - expoId: {}", expoId);
    }

    /**
     * 박람회 총 일수 계산
     * 
     * @param expo 박람회 엔티티
     * @return 총 일수
     */
    private int calculateTotalDays(Expo expo) {
        long days = ChronoUnit.DAYS.between(expo.getStartDate(), expo.getEndDate()) + 1;
        return (int) days;
    }

    /**
     * 박람회 총 금액 계산
     * 보증금 + (일일사용료 × 일수) + 프리미엄보증금
     * 
     * @param expo 박람회 엔티티
     * @param feeSetting 수수료 설정
     * @param totalDays 총 일수
     * @return 총 금액
     */
    private Integer calculateTotalAmount(Expo expo, ExpoFeeSetting feeSetting, int totalDays) {
        Integer deposit = feeSetting.getDeposit() != null ? feeSetting.getDeposit() : 0;
        Integer dailyUsageFee = feeSetting.getDailyUsageFee() != null ? feeSetting.getDailyUsageFee() : 0;
        Integer dailyFee = dailyUsageFee * totalDays;
        
        if (expo.getIsPremium()) {
            // 프리미엄일 경우: 기본 등록금 + 프리미엄 이용료 + 사용료
            Integer premiumDeposit = feeSetting.getPremiumDeposit() != null ? feeSetting.getPremiumDeposit() : 0;
            return deposit + premiumDeposit + dailyFee;
        } else {
            // 기본일 경우: 기본 등록금 + 사용료
            return deposit + dailyFee;
        }
    }
}