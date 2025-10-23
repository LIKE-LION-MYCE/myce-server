package com.myce.expo.repository;

import com.myce.expo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * QueryDSL을 사용한 Review 커스텀 쿼리 인터페이스
 */
public interface CustomReviewRepository {

    List<Review> searchReviews(Long expoId, Integer minRating, String keyword);

}
