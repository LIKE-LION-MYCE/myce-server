package com.myce.member.dto.expo;

import com.myce.expo.entity.type.ExpoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpoPaymentDetailResponse {
    
    // 박람회 기본 정보
    private String expoTitle;
    private String applicantName;  // 신청자 (회사명)
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private ExpoStatus status;
    
    // 결제 정보
    private Integer totalDays;         // 총 일수
    private Integer dailyUsageFee;     // 일당 사용료
    private Integer usageFeeAmount;    // 사용료 총액 (일당 * 총일수)
    private Integer depositAmount;     // 등록금 (프리미엄이면 premiumDeposit, 아니면 deposit)
    private Integer totalAmount;       // 총 결제해야 할 금액
    private Boolean isPremium;         // 프리미엄 여부
}