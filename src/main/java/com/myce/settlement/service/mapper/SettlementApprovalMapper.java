package com.myce.settlement.service.mapper;

import com.myce.member.entity.Member;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.entity.code.SettlementStatus;

/**
 * Settlement 정산 승인 매퍼
 * 플랫폼 관리자 정산 승인 관련 Settlement 엔티티 매핑을 담당
 */
public class SettlementApprovalMapper {
    
    /**
     * 정산 승인용 Settlement 엔티티 생성
     * 기존 Settlement를 승인 상태로 업데이트
     * 
     * @param existing 기존 Settlement 엔티티
     * @param adminMember 승인 처리하는 관리자
     * @return 승인 처리된 Settlement 엔티티
     */
    public static Settlement toApprovedEntity(Settlement existing, Member adminMember) {
        return Settlement.builder()
                .id(existing.getId())
                .expo(existing.getExpo())
                .adminMember(adminMember)
                .totalAmount(existing.getTotalAmount())
                .supplyAmount(existing.getSupplyAmount())
                .settleAmount(existing.getSettleAmount())
                .settlementStatus(SettlementStatus.APPROVED)
                .settlementAt(java.time.LocalDateTime.now())
                .receiverName(existing.getReceiverName())
                .bankName(existing.getBankName())
                .bankAccount(existing.getBankAccount())
                .createdAt(existing.getCreatedAt())
                .build();
    }
}