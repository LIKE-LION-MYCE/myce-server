package com.myce.expo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReviewListResponse {
    
    private List<ReviewResponse> reviews;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // 전체 리뷰 통계
    private Double averageRating;
    private RatingSummary ratingSummary;
    
    public ReviewListResponse(Page<ReviewResponse> page) {
        this.reviews = page.getContent();
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class RatingSummary {
        private Long fiveStars = 0L;
        private Long fourStars = 0L;
        private Long threeStars = 0L;
        private Long twoStars = 0L;
        private Long oneStars = 0L;
        
        public RatingSummary(Long fiveStars, Long fourStars, Long threeStars, Long twoStars, Long oneStars) {
            this.fiveStars = fiveStars != null ? fiveStars : 0L;
            this.fourStars = fourStars != null ? fourStars : 0L;
            this.threeStars = threeStars != null ? threeStars : 0L;
            this.twoStars = twoStars != null ? twoStars : 0L;
            this.oneStars = oneStars != null ? oneStars : 0L;
        }
    }
}