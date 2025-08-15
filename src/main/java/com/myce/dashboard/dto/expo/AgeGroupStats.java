package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class AgeGroupStats {
    private List<AgeGroup> ageGroups;
    
    @Builder
    public AgeGroupStats(List<AgeGroup> ageGroups) {
        this.ageGroups = ageGroups;
    }
    
    @Getter
    @NoArgsConstructor
    public static class AgeGroup {
        private String ageRange;
        private Long count;
        private Float percentage;
        
        @Builder
        public AgeGroup(String ageRange, Long count, Float percentage) {
            this.ageRange = ageRange;
            this.count = count;
            this.percentage = percentage;
        }
    }
}