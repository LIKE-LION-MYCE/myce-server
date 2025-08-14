package com.myce.dashboard.dto.platform;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RevenueChartData {
    private List<String> labels; // 예: ["2025/08/14", "2025/08/13", ...]
    private List<Long> data;

    @Builder
    public RevenueChartData(List<String> labels, List<Long> data) {
        this.labels = labels;
        this.data = data;
    }
}
