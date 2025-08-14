package com.myce.dashboard.service.platform;

import com.myce.dashboard.dto.platform.RevenueDashboardResponse;
import com.myce.dashboard.dto.platform.type.PeriodType;

public interface RevenueService {

    RevenueDashboardResponse getSettlementDashboard(PeriodType period, Long size);
}
