package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ExpoReviewsResponse {
    private Long expoId;
    private String expoTitle;
    private Double averageRating; // 평균 평점
    private Integer totalReviews; // 전체 리뷰 수
    private RatingSummary ratingSummary; // 별점별 개수
    private List<ReviewInfo> reviews; // 리뷰 목록
    
    @Getter
    @Builder
    public static class RatingSummary {
        private Integer fiveStars;
        private Integer fourStars;
        private Integer threeStars;
        private Integer twoStars;
        private Integer oneStars;
    }
    
    @Getter
    @Builder
    public static class ReviewInfo {
        private Long reviewId;
        private String memberName;
        private String title;
        private String content;
        private Integer rating;
        private LocalDateTime createdAt;
        private Boolean isMyReview; // 현재 사용자의 리뷰인지 여부
    }
}