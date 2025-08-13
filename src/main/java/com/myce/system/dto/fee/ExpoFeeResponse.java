package com.myce.system.dto.fee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpoFeeResponse {
    private final Long id;
    private final String name;
    private final int deposit;
    private final int premiumDeposit;
    private final BigDecimal settlementCommission;
    private final int dailyUsageFee;
    private final boolean isActive;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;
}
