package com.myce.dashboard.dto.platform;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RevenueDashboardResponse {
    private List<RevenueSummary> summaryItems;
    private RevenueChartData chartData;
    @Builder
    public RevenueDashboardResponse(List<RevenueSummary> summaryItems, RevenueChartData chartData) {
        this.summaryItems = summaryItems;
        this.chartData = chartData;
    }
}