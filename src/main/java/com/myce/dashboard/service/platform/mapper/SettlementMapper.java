package com.myce.dashboard.service.platform.mapper;

import com.myce.dashboard.dto.platform.RevenueSummary;

public class SettlementMapper {
    public static RevenueSummary toSummary(String label, long currentResult,
                                           float compareRatio, boolean isTrending) {
        return RevenueSummary.builder()
                .label(label)
                .value(currentResult)
                .change(compareRatio)
                .isTrending(isTrending)
                .build();
    }
}
