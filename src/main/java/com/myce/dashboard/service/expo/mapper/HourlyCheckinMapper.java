package com.myce.dashboard.service.expo.mapper;

import com.myce.dashboard.dto.expo.HourlyCheckin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HourlyCheckinMapper {

    /**
     * 시간대별 체크인 데이터 생성 (9시~18시)
     * @param cacheValueProvider 캐시에서 값을 가져오는 함수
     * @return 시간대별 체크인 리스트
     */
    public List<HourlyCheckin> createHourlyCheckins(java.util.function.Function<String, Long> cacheValueProvider) {
        List<HourlyCheckin> result = new ArrayList<>();
        
        for (int hour = 9; hour <= 18; hour++) {
            String timeRange = String.format("%02d:00", hour);
            Long count = cacheValueProvider.apply("checkin:hourly:" + hour);
            
            result.add(HourlyCheckin.builder()
                    .timeRange(timeRange)
                    .checkinCount(count != null ? count : 0L)
                    .build());
        }
        
        return result;
    }

    /**
     * 시간과 카운트로 HourlyCheckin 생성
     */
    public HourlyCheckin mapFromHourAndCount(int hour, Long count) {
        String timeRange = String.format("%02d:00", hour);
        
        return HourlyCheckin.builder()
                .timeRange(timeRange)
                .checkinCount(count != null ? count : 0L)
                .build();
    }
    
    /**
     * DB 쿼리 결과를 HourlyCheckin 리스트로 변환 (9시~18시)
     * @param queryResults QrCodeRepository.countHourlyCheckinsByExpoIdAndDate 결과
     * @return 시간대별 체크인 리스트
     */
    public List<HourlyCheckin> mapFromQueryResults(List<Object[]> queryResults) {
        // 쿼리 결과를 Map으로 변환 (hour -> count)
        Map<Integer, Long> hourCountMap = queryResults.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(), // hour
                        row -> ((Number) row[1]).longValue(), // count
                        (existing, replacement) -> existing
                ));
        
        List<HourlyCheckin> result = new ArrayList<>();
        
        // 9시~18시 범위로 데이터 생성
        for (int hour = 9; hour <= 18; hour++) {
            Long count = hourCountMap.getOrDefault(hour, 0L);
            result.add(mapFromHourAndCount(hour, count));
        }
        
        return result;
    }
}