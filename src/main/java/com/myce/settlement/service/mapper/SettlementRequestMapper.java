package com.myce.settlement.service.mapper;

import com.myce.expo.entity.Expo;
import com.myce.member.dto.expo.ExpoSettlementRequest;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.entity.code.SettlementStatus;

/**
 * Settlement 정산 신청 매퍼
 * 박람회 관리자 정산 신청 관련 Settlement 엔티티 매핑을 담당
 */
public class SettlementRequestMapper {
    
    /**
     * 정산 신청용 Settlement 엔티티 생성
     * 박람회 관리자가 계좌 정보를 입력하여 정산 신청할 때 사용
     * 
     * @param expo 박람회 엔티티
     * @param request 정산 신청 요청 데이터
     * @param receiptResponse 정산 영수증 데이터 (매출 계산 결과)
     * @return Settlement 엔티티
     */
    public static Settlement toRequestEntity(Expo expo, 
                                           ExpoSettlementRequest request, 
                                           ExpoSettlementReceiptResponse receiptResponse) {
        return Settlement.builder()
                .expo(expo)
                .adminMember(null) // 정산 승인 시 설정
                .totalAmount(receiptResponse.getTotalRevenue())
                .supplyAmount(receiptResponse.getCommissionAmount()) // 수수료 금액
                .settleAmount(receiptResponse.getNetProfit()) // 순수익
                .settlementStatus(SettlementStatus.PENDING)
                .settlementAt(null) // 정산 승인 시 설정
                .receiverName(request.getReceiverName())
                .bankName(request.getBankName())
                .bankAccount(request.getBankAccount())
                .build();
    }
}