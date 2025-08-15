package com.myce.dashboard.service.expo.mapper;

import com.myce.dashboard.dto.expo.GenderStats;
import com.myce.member.entity.type.Gender;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class GenderStatsMapper {

    /**
     * DB 쿼리 결과(Object[])를 GenderStats로 변환
     * @param genderResults DB에서 조회한 [성별, 카운트] 결과 리스트
     * @return 성별 통계 객체
     */
    public GenderStats mapFromQueryResults(List<Object[]> genderResults) {
        long maleCount = 0;
        long femaleCount = 0;
        long totalCount = 0;
        
        // 성별별 카운트 집계
        for (Object[] result : genderResults) {
            Gender gender = (Gender) result[0];
            Long count = ((Number) result[1]).longValue();
            
            totalCount += count;
            if (gender == Gender.MALE) {
                maleCount = count;
            } else if (gender == Gender.FEMALE) {
                femaleCount = count;
            }
        }
        
        // 비율 계산
        BigDecimal maleRatio = totalCount > 0 ? 
                BigDecimal.valueOf(maleCount * 100.0 / totalCount).setScale(1, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        BigDecimal femaleRatio = totalCount > 0 ? 
                BigDecimal.valueOf(femaleCount * 100.0 / totalCount).setScale(1, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        return GenderStats.builder()
                .maleCount(maleCount)
                .femaleCount(femaleCount)
                .malePercentage(maleRatio.floatValue())
                .femalePercentage(femaleRatio.floatValue())
                .build();
    }
}