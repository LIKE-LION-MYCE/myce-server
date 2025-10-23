package com.myce.querydsl.controller;

import com.myce.expo.dto.ReviewListResponse;
import com.myce.querydsl.service.QueryDslSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * QueryDSL 기반 동적 검색 API 컨트롤러
 *
 * 목적: 복잡한 동적 쿼리가 필요한 검색 기능을 제공
 *
 * 예시:
 * - 리뷰 검색 (평점, 키워드 필터링)
 * - 향후 확장 가능 (박람회 검색, 부스 검색 등)
 */
@RestController
@RequestMapping("/api/querydsl/search")
@RequiredArgsConstructor
public class QueryDslSearchController {

    private final QueryDslSearchService searchService;

    /**
     * 리뷰 동적 검색 (QueryDSL)
     *
     * @param expoId 박람회 ID (필수)
     * @param minRating 최소 평점 (선택, 1~5)
     * @param keyword 검색 키워드 (선택, 제목/내용 검색)
     * @return 검색된 리뷰 목록
     */
    @GetMapping("/reviews")
    public ResponseEntity<ReviewListResponse> searchReviews(
            @RequestParam Long expoId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String keyword) {

        ReviewListResponse response = searchService.searchReviews(expoId, minRating, keyword);
        return ResponseEntity.ok(response);
    }

}
