package com.myce.system.dto.fee;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private final boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime updatedAt;
}