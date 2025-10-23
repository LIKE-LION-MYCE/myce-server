package com.myce.querydsl.service;

import com.myce.expo.dto.ReviewListResponse;

/**
 * QueryDSL 기반 검색 서비스 인터페이스
 *
 * 목적:
 * - 복잡한 동적 쿼리를 QueryDSL로 처리
 * - 기존 Service와 분리하여 검색 로직을 독립적으로 관리
 */
public interface QueryDslSearchService {

    /**
     * QueryDSL을 사용한 리뷰 동적 검색
     *
     * @param expoId 박람회 ID (필수)
     * @param minRating 최소 평점 (선택, null이면 조건 무시)
     * @param keyword 검색 키워드 (선택, null이면 조건 무시)
     * @return 검색된 리뷰 목록
     */
    ReviewListResponse searchReviews(Long expoId, Integer minRating, String keyword);
}
