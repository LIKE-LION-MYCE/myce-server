package com.myce.expo.repository;

import com.myce.expo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 박람회별 리뷰 목록 조회 (페이징)
    Page<Review> findByExpoIdOrderByCreatedAtDesc(Long expoId, Pageable pageable);
    
    // 박람회별 평균 평점 계산
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.expo.id = :expoId")
    Double findAverageRatingByExpoId(@Param("expoId") Long expoId);
    
    // 박람회별 전체 리뷰 개수
    Long countByExpoId(Long expoId);
    
    // 박람회별 별점별 개수
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.expo.id = :expoId GROUP BY r.rating")
    Object[][] findRatingCountByExpoId(@Param("expoId") Long expoId);
}