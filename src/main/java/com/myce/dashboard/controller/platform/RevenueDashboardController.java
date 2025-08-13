package com.myce.dashboard.controller.platform;

import com.myce.dashboard.dto.platform.RevenueDashboardResponse;
import com.myce.dashboard.dto.platform.type.PeriodType;
import com.myce.dashboard.service.platform.RevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class RevenueDashboardController {
    private final RevenueService revenueService;

    private final Long SIZE = 8L;

    @GetMapping("/revenue")
    public RevenueDashboardResponse getRevenueDashboardData(@RequestParam String period) {
        PeriodType periodType = PeriodType.fromLabel(period);

        return revenueService.getSettlementDashboard(periodType, SIZE);
    }
}