package com.myce.expo.dto;

import com.myce.expo.entity.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReviewResponse {
    
    private Long id;
    private Long expoId;
    private String expoTitle;
    private Long memberId;
    private String memberName;
    private String title;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.expoId = review.getExpo().getId();
        this.expoTitle = review.getExpo().getTitle();
        this.memberId = review.getMember().getId();
        this.memberName = review.getMember().getName();
        this.title = review.getTitle();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }
}