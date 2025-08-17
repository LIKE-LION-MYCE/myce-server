package com.myce.dashboard.service.platform.impl;

import com.myce.dashboard.dto.platform.DashboardChartData;
import com.myce.dashboard.dto.platform.DashboardSummary;
import com.myce.dashboard.dto.platform.RevenueDashboardResponse;
import com.myce.dashboard.dto.platform.type.PeriodType;
import com.myce.dashboard.record.CheckDivideZero;
import com.myce.dashboard.service.platform.RevenueService;
import com.myce.dashboard.service.platform.mapper.PlatformDashboardMapper;
import com.myce.dashboard.util.ChartUtil;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.settlement.entity.code.SettlementStatus;
import com.myce.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.myce.dashboard.util.ComparisonUtil.getCheckDivideZero;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {
    private final SettlementRepository settlementRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;

    public RevenueDashboardResponse getSettlementDashboard(PeriodType period, Long size) {
        Long periodTime = PeriodType.getNumberOfDays(period);

        List<DashboardSummary> settlementSummaries = gatherSummary(periodTime);
        DashboardChartData chartData = getChartData(periodTime, size);

        return RevenueDashboardResponse.builder()
                .summaryItems(settlementSummaries)
                .chartData(chartData)
                .build();
    }

    public List<DashboardSummary> gatherSummary(Long periodTime) {
        return List.of(
                getTotalSettlement(periodTime),
                getExpoBenefit(periodTime),
                getAdBenefit(periodTime),
                getTotalBenefit(periodTime)
        );
    }

    public DashboardSummary getTotalSettlement(Long period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(period);

        Long currentResult = settlementRepository
                .countSettlementBySettlementAtBetweenAndSettlementStatus(startDate, endDate,
                        SettlementStatus.APPROVED);
        Long pastResult = settlementRepository
                .countSettlementBySettlementAtBetweenAndSettlementStatus(startDate.minusDays(period),
                        endDate.minusDays(period), SettlementStatus.APPROVED);

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("총 정산 수", currentResult,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardSummary getExpoBenefit(Long period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(period);

        long currentResult = getTotalExpoBenefitInPeriod(startDate, endDate);
        long pastResult = getTotalExpoBenefitInPeriod(startDate.minusDays(period), endDate.minusDays(period));

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("박람회 순수익", currentResult,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardSummary getAdBenefit(Long period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(period);

        long currentResult = getTotalAdBenefitInPeriod(startDate, endDate);
        long pastResult = getTotalAdBenefitInPeriod(startDate.minusDays(period), endDate.minusDays(period));

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("광고 수익", currentResult,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardSummary getTotalBenefit(Long period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(period);

        long currentResult = getTotalBenefitInPeriod(startDate, endDate);
        long pastResult = getTotalBenefitInPeriod(startDate.minusDays(period), endDate.minusDays(period));

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("총 수익", currentResult, comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardChartData getChartData(Long period, Long size) {
        List<Long> data = new ArrayList<>();
        LocalDateTime endDate = LocalDateTime.now();

        for (int i = 0; i < size; i++) {
            LocalDateTime startDate = endDate.minusDays(period);
            long totalBenefit = getTotalBenefitInPeriod(startDate, endDate);
            data.add(totalBenefit);
            endDate = startDate;
        }

        return ChartUtil.getDashboardChartData(period, size, data);
    }

    private long getTotalBenefitInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        long expoBenefit = getTotalExpoBenefitInPeriod(startDate, endDate);
        long adBenefit = getTotalAdBenefitInPeriod(startDate, endDate);
        return expoBenefit + adBenefit;
    }

    private long getTotalExpoBenefitInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        Long ticketBenefit = Optional.ofNullable(settlementRepository
                        .sumRevenueByStatusAndUpdatedAtBetween(SettlementStatus.APPROVED, startDate, endDate))
                .orElse(0L);

        Long applyDeposit = Optional.ofNullable(expoPaymentInfoRepository
                        .sumTotalAmountByStatusesAndUpdatedAtBetween(PaymentStatus.getPaidStatusList(), startDate, endDate))
                .orElse(0L);

        return ticketBenefit + applyDeposit;
    }

    private long getTotalAdBenefitInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return Optional.ofNullable(adPaymentInfoRepository
                        .sumTotalAmountByStatusAndUpdatedAtBetween(PaymentStatus.getPaidStatusList(), startDate, endDate))
                .orElse(0L);
    }
}