package com.myce.dashboard.controller.platform;

import com.myce.dashboard.dto.platform.RevenueDashboardResponse;
import com.myce.dashboard.dto.platform.UsageDashboardResponse;
import com.myce.dashboard.dto.platform.type.PeriodType;
import com.myce.dashboard.service.platform.RevenueService;
import com.myce.dashboard.service.platform.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
@Slf4j
public class PlatformDashboardController {
    private final RevenueService revenueService;
    private final UsageService usageService;

    private final Long CHART_SIZE = 8L;

    @GetMapping("/revenue")
    public RevenueDashboardResponse getRevenueDashboardData(@RequestParam String period) {
        PeriodType periodType = PeriodType.fromLabel(period);
        log.info("정산 대시보드 조회 시작 : {}", period);

        return revenueService.getSettlementDashboard(periodType, CHART_SIZE);
    }

    @GetMapping("/usage")
    public UsageDashboardResponse getUsageDashboardData(@RequestParam String period) {
        PeriodType periodType = PeriodType.fromLabel(period);
        log.info("사용량 대시보드 조회 시작 : {}", period);

        return usageService.getUsageDashboard(periodType, CHART_SIZE);
    }
}