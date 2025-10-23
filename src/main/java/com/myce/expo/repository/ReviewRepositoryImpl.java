package com.myce.expo.repository;

import com.myce.expo.entity.QReview;
import com.myce.expo.entity.Review;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QueryDSL을 사용한 Review 쿼리 구현체
 * 네이밍 규칙: Repository 인터페이스명 + Impl (Spring Data JPA가 자동 인식)
 */
@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory queryFactory;
    private static final QReview review = QReview.review;

    @Override
    public List<Review> searchReviews(Long expoId, Integer minRating, String keyword) {

        // 쿼리 작성
        List<Review> results = queryFactory
                .selectFrom(review)
                .where(
                    expoIdEq(expoId),           // 박람회 ID 필수 조건
                    ratingGoe(minRating),        // 최소 평점 조건
                    keywordContains(keyword)   // 키워드 검색 조건
                )
                .orderBy(review.createdAt.desc())  // 최신순 정렬
                .fetch();

        return results;
    }


    /**
     * 박람회 ID 조건 (필수)
     */
    private BooleanExpression expoIdEq(Long expoId) {
        return expoId != null ? review.expo.id.eq(expoId) : null;
    }

    /**
     * 최소 평점 조건 (선택)
     */
    private BooleanExpression ratingGoe(Integer minRating) {
        return minRating != null ? review.rating.goe(minRating) : null;
    }

    /**
     * 키워드 검색 조건 - 제목 또는 내용에 포함 (선택)
     */
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return review.title.containsIgnoreCase(keyword)
                .or(review.content.containsIgnoreCase(keyword));
    }
}
