package com.myce.dashboard.service.platform.impl;

import com.myce.advertisement.repository.AdRepository;
import com.myce.dashboard.dto.platform.DashboardChartData;
import com.myce.dashboard.dto.platform.DashboardSummary;
import com.myce.dashboard.dto.platform.UsageDashboardResponse;
import com.myce.dashboard.dto.platform.type.PeriodType;
import com.myce.dashboard.record.CheckDivideZero;
import com.myce.dashboard.service.platform.UsageService;
import com.myce.dashboard.service.platform.mapper.PlatformDashboardMapper;
import com.myce.dashboard.util.ChartUtil;
import com.myce.dashboard.util.ComparisonUtil;
import com.myce.expo.repository.ExpoRepository;
import com.myce.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {
    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;
    private final AdRepository adRepository;

    public UsageDashboardResponse getUsageDashboard(PeriodType period, long chartSize) {
        long periodTime = PeriodType.getNumberOfDays(period);

        return UsageDashboardResponse.builder()
                .summaryItems(gatherUsageSummary(periodTime))
                .chartData((HashMap<String, DashboardChartData>)
                        gatherChartData(periodTime, chartSize))
                .build();
    }

    // gather summary

    private List<DashboardSummary> gatherUsageSummary(Long periodTime){

        return List.of(
                getTotalExpos(periodTime),
                getTotalReservation(periodTime),
                getTotalAdApply(periodTime)
        );
    }

    private DashboardSummary getTotalExpos(Long periodTime) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(periodTime);

        long currentCount = expoRepository.countAllAfterDate(timestamp.toLocalDate());
        long pastCount = expoRepository.countAllAfterDate(timestamp.minusDays(periodTime).toLocalDate());

        CheckDivideZero comparisonInfo = ComparisonUtil.getCheckDivideZero(pastCount, currentCount);

        return PlatformDashboardMapper.toSummary("누적 행사 수", currentCount,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    private DashboardSummary getTotalReservation(Long period) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(period);

        long currentCount = reservationRepository.countAllByCreatedAtAfter(timestamp);
        long pastCount = reservationRepository.countAllByCreatedAtAfter(timestamp.minusDays(period));

        CheckDivideZero comparisonInfo = ComparisonUtil.getCheckDivideZero(pastCount, currentCount);

        return PlatformDashboardMapper.toSummary("누적 예약수", currentCount,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    private DashboardSummary getTotalAdApply(Long period) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(period);

        long currentCount = adRepository.countAllByDateAfter(timestamp.toLocalDate());
        long pastCount = adRepository.countAllByDateAfter(timestamp.minusDays(period).toLocalDate());

        CheckDivideZero comparisonInfo = ComparisonUtil.getCheckDivideZero(pastCount, currentCount);

        return PlatformDashboardMapper.toSummary("누적 광고수", currentCount,
                comparisonInfo.compareRatio(), comparisonInfo.isTrending());
    }

    // gather chart data


    private Map<String, DashboardChartData> gatherChartData(long period, long size) {
        Map<String, DashboardChartData> chartData = new HashMap<>();
        chartData.put("expo", getExpoChartData(period, size));
        chartData.put("reservation", getReservationChartData(period, size));
        chartData.put("ad", getAdChartData(period, size));
        return chartData;
    }

    private DashboardChartData getExpoChartData(long periodTime, long size) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(periodTime);
        List<Long> data = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            long currentCount = expoRepository.countAllAfterDate(timestamp.toLocalDate());
            long pastCount = expoRepository.countAllAfterDate(timestamp.minusDays(periodTime).toLocalDate());
            data.add(pastCount - currentCount);
            timestamp = timestamp.minusDays(periodTime);
        }
        return ChartUtil.getDashboardChartData(periodTime, size, data);
    }

    private DashboardChartData getReservationChartData(long periodTime, long size) {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(periodTime);
        List<Long> data = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            long currentCount = reservationRepository.countAllByCreatedAtAfter(timestamp);
            long pastCount = reservationRepository.countAllByCreatedAtAfter(timestamp.minusDays(periodTime));
            data.add(pastCount - currentCount);
            timestamp = timestamp.minusDays(periodTime);
        }
        return ChartUtil.getDashboardChartData(periodTime, size, data);
    }

    private DashboardChartData getAdChartData(long periodTime, long size){
        LocalDateTime timestamp = LocalDateTime.now().minusDays(periodTime);
        List<Long> data = new ArrayList<>();
        for(int i = 1; i<= size; i++){
            long currentCount = adRepository.countAllByDateAfter(timestamp.toLocalDate());
            long pastCount = adRepository.countAllByDateAfter(timestamp.minusDays(periodTime).toLocalDate());
            data.add(pastCount - currentCount);
            timestamp = timestamp.minusDays(periodTime);
        }
        return ChartUtil.getDashboardChartData(periodTime, size, data);
    }


}
