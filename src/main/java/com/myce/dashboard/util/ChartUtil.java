package com.myce.dashboard.util;

import com.myce.dashboard.dto.platform.DashboardChartData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartUtil {
    public static DashboardChartData getDashboardChartData(Long period, Long size, List<Long> data) {
        List<String> labels = new ArrayList<>();
        LocalDateTime timestamp;
        timestamp = LocalDateTime.now();

        for(int i = 1; i<= size; i++){
            labels.add(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE));
            timestamp = timestamp.minusDays(period);
        }

        Collections.reverse(labels);
        Collections.reverse(data);

        return DashboardChartData.builder()
                .labels(labels)
                .data(data)
                .build();
    }
}
