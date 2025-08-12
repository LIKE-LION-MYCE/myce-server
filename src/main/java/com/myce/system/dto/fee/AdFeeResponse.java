package com.myce.system.dto.fee;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdFeeResponse {
    private Long id;
    private String position;
    private String name;
    private int feePerDay;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
