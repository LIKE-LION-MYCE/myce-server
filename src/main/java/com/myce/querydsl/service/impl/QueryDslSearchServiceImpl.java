package com.myce.querydsl.service.impl;

import com.myce.expo.dto.ReviewListResponse;
import com.myce.expo.dto.ReviewResponse;
import com.myce.expo.entity.Review;
import com.myce.expo.repository.ReviewRepository;
import com.myce.querydsl.service.QueryDslSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * QueryDSL 기반 검색 서비스 구현체
 * 특징:
 * - Repository의 QueryDSL 구현체(CustomReviewRepository)를 활용
 * - 동적 조건: null 파라미터는 자동으로 무시됨
 * QueryDSL 동작 방식:
 * - expoId=1, minRating=null, keyword=null
 *   → WHERE expo_id = 1
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryDslSearchServiceImpl implements QueryDslSearchService {

    private final ReviewRepository reviewRepository;

    @Override
    public ReviewListResponse searchReviews(Long expoId, Integer minRating, String keyword) {

        // QueryDSL 동적 쿼리 실행 (CustomReviewRepository 구현체 사용)
        List<Review> reviews = reviewRepository.searchReviews(expoId, minRating, keyword);

        // Entity → DTO 변환
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(ReviewResponse::new)
                .toList();

        ReviewListResponse response = new ReviewListResponse(reviewResponses);

        // 평균 평점 추가
        Double averageRating = reviewRepository.findAverageRatingByExpoId(expoId);
        response.setAverageRating(averageRating != null ? averageRating : 0.0);

        return response;
    }

}
