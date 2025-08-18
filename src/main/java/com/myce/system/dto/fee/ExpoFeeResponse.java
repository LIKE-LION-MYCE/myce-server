package com.myce.system.dto.fee;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime updateTime;
}
