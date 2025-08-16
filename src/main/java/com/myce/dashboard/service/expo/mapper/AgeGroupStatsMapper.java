package com.myce.dashboard.service.expo.mapper;

import com.myce.dashboard.dto.expo.AgeGroupStats;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgeGroupStatsMapper {

    /**
     * DB 쿼리 결과(Object[])를 AgeGroupStats로 변환
     * @param ageResults DB에서 조회한 [연령대, 카운트] 결과 리스트
     * @return 연령대 통계 객체
     */
    public AgeGroupStats mapFromQueryResults(List<Object[]> ageResults) {
        // 전체 카운트 계산
        long totalCount = ageResults.stream()
                .mapToLong(result -> ((Number) result[1]).longValue())
                .sum();
        
        List<AgeGroupStats.AgeGroup> ageGroups = new ArrayList<>();
        
        // 각 연령대별 AgeGroup 객체 생성
        for (Object[] result : ageResults) {
            String ageRange = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            
            // 비율 계산
            Float percentage = totalCount > 0 ? 
                    (float) (count * 100.0 / totalCount) : 0.0f;
            
            ageGroups.add(AgeGroupStats.AgeGroup.builder()
                    .ageRange(ageRange)
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        
        return AgeGroupStats.builder()
                .ageGroups(ageGroups)
                .build();
    }
}