package com.myce.dashboard.service.platform.mapper;

import com.myce.dashboard.dto.platform.DashboardSummary;

public class PlatformDashboardMapper {
    public static DashboardSummary toSummary(String label, long currentResult,
                                             float compareRatio, boolean isTrending) {
        return DashboardSummary.builder()
                .label(label)
                .value(currentResult)
                .change(compareRatio)
                .isTrending(isTrending)
                .build();
    }
}
