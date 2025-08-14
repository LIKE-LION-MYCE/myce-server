package com.myce.member.dto.expo;

import com.myce.expo.entity.type.ExpoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpoSettlementReceiptResponse {
    
    // 박람회 기본 정보
    private String expoTitle;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private ExpoStatus status;
    
    // 티켓별 판매 정보
    private List<TicketSalesInfo> ticketSales;
    
    // 정산 정보
    private Integer totalRevenue;           // 총 매출 (모든 티켓 판매금액 합계)
    private BigDecimal commissionRate;      // 수수료율 (퍼센트)
    private Integer commissionAmount;       // 수수료 금액
    private Integer netProfit;              // 순수익 (총매출 - 수수료)
    
    // 정산 완료 정보 (COMPLETED 상태일 때만)
    private String receiverName;            // 정산 계좌 예금주 (settlement.receiver_name)
    private String bankName;                // 은행명 (settlement.bank_name)
    private String bankAccount;             // 계좌번호 (settlement.bank_account)
    private String settlementAt;            // 정산 승인 시점 (settlement.settlement_at)
    private String adminName;               // 정산 처리 담당자 이름 (member.name via settlement.admin_member_id)
    
    // 영수증 정보
    private LocalDate issueDate;            // 영수증 발행일 (오늘)
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketSalesInfo {
        private Long ticketId;
        private String ticketName;          // 티켓명
        private Integer ticketPrice;        // 티켓 단가
        private Integer soldCount;          // 판매된 수량
        private Integer totalSales;         // 티켓별 총 판매금액 (단가 * 수량)
    }
}