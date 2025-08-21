package com.myce.dashboard.service.platform.impl;

import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.dashboard.dto.platform.DashboardChartData;
import com.myce.dashboard.dto.platform.DashboardSummary;
import com.myce.dashboard.dto.platform.RevenueDashboardResponse;
import com.myce.dashboard.dto.platform.type.PeriodType;
import com.myce.dashboard.record.CheckDivideZero;
import com.myce.dashboard.service.platform.RevenueService;
import com.myce.dashboard.service.platform.mapper.PlatformDashboardMapper;
import com.myce.dashboard.util.ChartUtil;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.settlement.entity.code.SettlementStatus;
import com.myce.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.myce.dashboard.util.ComparisonUtil.getCheckDivideZero;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueServiceImpl implements RevenueService {
    private final SettlementRepository settlementRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final RefundRepository refundRepository;

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
                getExpoBenefit(periodTime),
                getAdBenefit(periodTime),
                getTotalExpoRefund(periodTime),
                getTotalAdRefund(periodTime),
                getTotalSettlement(periodTime),
                getTotalBenefit(periodTime)
        );
    }

    public DashboardSummary getTotalSettlement(Long period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(period);
        log.info("getTotalSettlement: startDate: {}, endDate: {}", startDate, endDate);
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

        return PlatformDashboardMapper.toSummary("박람회 수익", currentResult,
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

    public DashboardSummary getTotalExpoRefund(Long period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(period);

        long currentResult = getTotalExpoRefundInPeriod(startDate, endDate);
        long pastResult = getTotalExpoRefundInPeriod(startDate.minusDays(1), endDate.minusDays(1));

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult, currentResult);

        return PlatformDashboardMapper.toSummary("박람회 환불", currentResult, comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    public DashboardSummary getTotalAdRefund(Long period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(period);

        long currentResult = getTotalAdRefundInPeriod(startDate, endDate);
        long pastResult = getTotalAdRefundInPeriod(startDate.minusDays(1), endDate.minusDays(1));

        CheckDivideZero comparisonInfo = getCheckDivideZero(pastResult,currentResult);

        return PlatformDashboardMapper.toSummary("광고 환불", currentResult, comparisonInfo.compareRatio(), comparisonInfo.isTrending());
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

    private long getTotalExpoRefundInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return Optional.ofNullable(refundRepository.sumRefundAmountByTypeAndRefundedAtBetween(PaymentTargetType.EXPO, startDate, endDate))
                .orElse(0L);
    }

    private long getTotalAdRefundInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return Optional.ofNullable(refundRepository.sumRefundAmountByTypeAndRefundedAtBetween(PaymentTargetType.AD, startDate, endDate))
                .orElse(0L);
    }

    private long getTotalBenefitInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        long expoBenefit = getTotalExpoBenefitInPeriod(startDate, endDate);
        long adBenefit = getTotalAdBenefitInPeriod(startDate, endDate);
        long expoRefund = getTotalExpoRefundInPeriod(startDate, endDate);
        long adRefund = getTotalAdRefundInPeriod(startDate, endDate);
        return expoBenefit + adBenefit - expoRefund - adRefund;
    }

    private long getTotalExpoBenefitInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        Long ticketBenefit = Optional.ofNullable(settlementRepository
                        .sumRevenueByStatusAndUpdatedAtBetween(ExpoStatus.ACTIVE_STATUSES, startDate, endDate))
                .orElse(0L);
        Long applyDeposit = Optional.ofNullable(expoPaymentInfoRepository
                        .sumTotalAmountByStatusesAndUpdatedAtBetween(ExpoStatus.ACTIVE_STATUSES, startDate, endDate))
                .orElse(0L);

        return ticketBenefit + applyDeposit;
    }

    private long getTotalAdBenefitInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        Long adBenefit = Optional.ofNullable(adPaymentInfoRepository
                        .sumTotalAmountByStatusAndUpdatedAtBetween(AdvertisementStatus.ADMIN_VIEWABLE_STATUSES, startDate, endDate))
                .orElse(0L);

        return adBenefit;
    }
}