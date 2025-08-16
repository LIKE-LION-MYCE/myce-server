package com.myce.system.dto.fee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefundFeeResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String standardType;
    private final int standardDayCount;
    private final BigDecimal feeRate;
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}