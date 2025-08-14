package com.myce.dashboard.service.platform;

import com.myce.dashboard.dto.platform.UsageDashboardResponse;
import com.myce.dashboard.dto.platform.type.PeriodType;

public interface UsageService {
    UsageDashboardResponse getUsageDashboard(PeriodType period, long chartSize);
}
