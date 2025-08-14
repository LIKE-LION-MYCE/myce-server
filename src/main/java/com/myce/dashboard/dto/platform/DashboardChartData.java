package com.myce.dashboard.dto.platform;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DashboardChartData {
    private List<String> labels;
    private List<Long> data;

    @Builder
    public DashboardChartData(List<String> labels, List<Long> data) {
        this.labels = labels;
        this.data = data;
    }
}
