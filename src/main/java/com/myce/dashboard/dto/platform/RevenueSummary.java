package com.myce.dashboard.dto.platform;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RevenueSummary {
    private String label;
    private Long value;
    private Float change;
    private boolean isTrending;
    @Builder
    public RevenueSummary(String label, Long value, Float change, boolean isTrending) {
        this.label = label;
        this.value = value;
        this.change = change;
        this.isTrending = isTrending;
    }
}