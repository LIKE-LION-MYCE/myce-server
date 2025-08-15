package com.myce.settlement.service.mapper;

import com.myce.expo.entity.Expo;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.entity.code.SettlementStatus;

/**
 * Settlement 시스템/스케줄러 매퍼
 * 자동 생성 관련 Settlement 엔티티 매핑을 담당
 */
public class SettlementSystemMapper {
    
    /**
     * 초기 Settlement 엔티티 생성 (자동 생성용)
     * 스케줄러가 박람회 종료 시 호출하는 매핑
     * 
     * @param expo 종료된 박람회
     * @param totalRevenue 총 매출 (현재는 0)
     * @param commissionAmount 수수료 금액
     * @return Settlement 엔티티
     */
    public static Settlement toInitialEntity(Expo expo, Integer totalRevenue, Integer commissionAmount) {
        Integer netProfit = totalRevenue - commissionAmount;
        
        return Settlement.builder()
                .expo(expo)
                .adminMember(null) // 정산 승인 시 설정
                .totalAmount(totalRevenue)
                .supplyAmount(commissionAmount)
                .settleAmount(netProfit)
                .settlementStatus(SettlementStatus.PENDING)
                .settlementAt(null) // 정산 승인 시 설정
                .receiverName(null) // 박람회 관리자가 정산 신청 시 설정
                .bankName(null)     // 박람회 관리자가 정산 신청 시 설정
                .bankAccount(null)  // 박람회 관리자가 정산 신청 시 설정
                .build();
    }
}