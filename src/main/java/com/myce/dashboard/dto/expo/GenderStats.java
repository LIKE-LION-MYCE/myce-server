package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GenderStats {
    private Long maleCount;
    private Long femaleCount;
    private Float malePercentage;
    private Float femalePercentage;
    
    @Builder
    public GenderStats(Long maleCount, Long femaleCount, Float malePercentage, Float femalePercentage) {
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
        this.malePercentage = malePercentage;
        this.femalePercentage = femalePercentage;
    }
}