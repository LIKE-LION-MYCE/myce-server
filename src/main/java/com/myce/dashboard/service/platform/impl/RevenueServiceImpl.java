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

    public RevenueDashboardResponse getSettlementDashboard(PeriodType period, Long size){
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
        LocalDateTime timestamp = LocalDateTime.now().minusDays(period);

        Long currentResult = settlementRepository
                .countSettlementBySettlementAtAfterAndSettlementStatus(timestamp, SettlementStatus.PAID);
        Long pastResult = settlementRepository
                .countSettlementBySettlementAtAfterAndSettlementStatus(timestamp.minusDays(period), SettlementStatus.PAID);

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("총 정산 수", currentResult,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    // 박람회 등록금 + 티켓 수익(토나오는)
    // (진짜토나오는)
    public DashboardSummary getExpoBenefit(Long period) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(period);

        // 현재 단위 총 수입
        totalExpoBenefit current = getTotalExpoBenefit(timestamp);
        // 과거 단위 총 수입
        totalExpoBenefit past = getTotalExpoBenefit(timestamp.minusDays(period));

        Long currentResult = current.ticketBenefit() + current.applyDeposit();
        Long pastResult = past.ticketBenefit() + past.applyDeposit();

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("박람회 순수익", currentResult,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardSummary getAdBenefit(Long period) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(period);

        totalAdBenefit adBenefit = getTotalAdBenefit(period, timestamp);

        CheckDivideZero comparisonInfo = getCheckDivideZero(adBenefit.pastResult,
                adBenefit.currentResult);

        return PlatformDashboardMapper.toSummary("광고 수익", adBenefit.currentResult(),
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardSummary getTotalBenefit(Long period) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(period);
        totalExpoBenefit currentExpo = getTotalExpoBenefit(timestamp);
        totalExpoBenefit pastExpo = getTotalExpoBenefit(timestamp.minusDays(period));
        totalAdBenefit adBenefit = getTotalAdBenefit(period, timestamp);

        Long currentResult = currentExpo.ticketBenefit() + currentExpo.applyDeposit() + adBenefit.currentResult();
        Long pastResult = pastExpo.ticketBenefit() + pastExpo.applyDeposit() + adBenefit.pastResult();

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("총 수익", currentResult, comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardChartData getChartData(Long period, Long size) {
        LocalDateTime timestamp = LocalDateTime.now();
        List<Long> data = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            totalExpoBenefit currentExpo = getTotalExpoBenefit(timestamp);
            totalExpoBenefit pastExpo = getTotalExpoBenefit(timestamp.minusDays(period));
            totalAdBenefit adBenefit = getTotalAdBenefit(period, timestamp);

            long current = currentExpo.ticketBenefit() + currentExpo.applyDeposit() + adBenefit.currentResult;
            long past = pastExpo.ticketBenefit() + pastExpo.applyDeposit() + adBenefit.pastResult;

            data.add(past - current);
            timestamp = timestamp.minusDays(period);
        }
        return ChartUtil.getDashboardChartData(period, size, data);
    }

    // 전체 박람회 수익 계산(티켓 수수료 + 등록금)
    private totalExpoBenefit getTotalExpoBenefit(LocalDateTime timestamp) {
        Long ticketBenefit = Optional.ofNullable(settlementRepository
                        .sumRevenueByStatusAndUpdatedAtAfter(SettlementStatus.PAID, timestamp))
                .orElse(0L);
        Long applyDeposit = Optional.ofNullable(expoPaymentInfoRepository
                        .sumTotalAmountByStatusesAndUpdatedAtAfter(PaymentStatus.getPaidStatusList(),
                                timestamp))
                .orElse(0L);
        return new totalExpoBenefit(ticketBenefit, applyDeposit);
    }

    private record totalExpoBenefit(Long ticketBenefit, Long applyDeposit) {
    }

    // 전체 광고 수익 계산(등록금)
    private totalAdBenefit getTotalAdBenefit(Long period, LocalDateTime timestamp) {
        Long currentResult = Optional.ofNullable(adPaymentInfoRepository
                .sumTotalAmountByStatusAndUpdatedAtAfter(PaymentStatus.getPaidStatusList(), timestamp))
                .orElse(0L);
        Long pastResult = Optional.ofNullable(adPaymentInfoRepository
                .sumTotalAmountByStatusAndUpdatedAtAfter(PaymentStatus.getPaidStatusList(), timestamp.minusDays(period)))
                .orElse(0L);
        totalAdBenefit result = new totalAdBenefit(currentResult, pastResult);
        return result;
    }

    private record totalAdBenefit(long currentResult, long pastResult) {
    }
}
