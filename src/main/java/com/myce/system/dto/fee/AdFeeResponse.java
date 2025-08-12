package com.myce.system.dto.fee;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdFeeResponse {
    private final Long id;
    private final String position;
    private final String name;
    private final int feePerDay;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
