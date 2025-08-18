package com.myce.expo.service;

import com.myce.expo.dto.*;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    
    ReviewResponse createReview(ReviewCreateRequest request, Long memberId);
    
    ReviewListResponse getReviewsByExpo(Long expoId, String sortBy, Pageable pageable);
    
    ReviewResponse getReviewById(Long reviewId);
    
    ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long memberId);
    
    void deleteReview(Long reviewId, Long memberId);
    
    ReviewListResponse getMyReviews(Long memberId, Pageable pageable);
    
    boolean hasUserAttendedExpo(Long memberId, Long expoId);
    
    boolean hasUserReviewedExpo(Long memberId, Long expoId);
    
    ReviewListResponse getBestReviews(int limit);
}