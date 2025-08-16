package com.myce.dashboard.dto.platform;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Getter
@NoArgsConstructor
public class UsageDashboardResponse {
    private List<DashboardSummary> summaryItems;
    private HashMap<String, DashboardChartData> chartData;

    @Builder
    public UsageDashboardResponse(List<DashboardSummary> summaryItems,
                                  HashMap<String, DashboardChartData> chartData) {
        this.summaryItems = summaryItems;
        this.chartData = chartData;
    }
}
