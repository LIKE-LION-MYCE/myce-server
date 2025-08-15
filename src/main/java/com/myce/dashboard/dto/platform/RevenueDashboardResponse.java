package com.myce.dashboard.dto.platform;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RevenueDashboardResponse {
    private List<DashboardSummary> summaryItems;
    private DashboardChartData chartData;
    @Builder
    public RevenueDashboardResponse(List<DashboardSummary> summaryItems, DashboardChartData chartData) {
        this.summaryItems = summaryItems;
        this.chartData = chartData;
    }
}