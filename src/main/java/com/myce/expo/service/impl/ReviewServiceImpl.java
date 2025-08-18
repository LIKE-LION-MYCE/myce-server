package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.*;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Review;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.ReviewRepository;
import com.myce.expo.service.ReviewService;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.qrcode.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ExpoRepository expoRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final QrCodeRepository qrCodeRepository;
    
    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, Long memberId) {
        // 박람회 존재 확인
        Expo expo = expoRepository.findById(request.getExpoId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        
        // 박람회 참석 여부 확인
        if (!hasUserAttendedExpo(memberId, request.getExpoId())) {
            throw new CustomException(CustomErrorCode.REVIEW_UNAUTHORIZED_NOT_ATTENDED);
        }
        
        // 이미 리뷰를 작성했는지 확인
        if (hasUserReviewedExpo(memberId, request.getExpoId())) {
            throw new CustomException(CustomErrorCode.REVIEW_ALREADY_EXISTS);
        }
        
        Review review = new Review(expo, member, request.getTitle(), request.getContent(), request.getRating());
        Review savedReview = reviewRepository.save(review);
        
        return new ReviewResponse(savedReview);
    }
    
    @Override
    public ReviewListResponse getReviewsByExpo(Long expoId, String sortBy, Pageable pageable) {
        Page<Review> reviews;
        
        if ("rating".equals(sortBy)) {
            reviews = reviewRepository.findByExpoIdOrderByRatingDesc(expoId, pageable);
        } else {
            reviews = reviewRepository.findByExpoIdOrderByCreatedAtDesc(expoId, pageable);
        }
        
        Page<ReviewResponse> reviewResponses = reviews.map(ReviewResponse::new);
        ReviewListResponse response = new ReviewListResponse(reviewResponses);
        
        // 전체 리뷰 통계 추가
        Double averageRating = reviewRepository.findAverageRatingByExpoId(expoId);
        response.setAverageRating(averageRating != null ? averageRating : 0.0);
        
        // 별점별 분포 계산
        Object[][] ratingCounts = reviewRepository.findRatingCountByExpoId(expoId);
        Long fiveStars = 0L, fourStars = 0L, threeStars = 0L, twoStars = 0L, oneStars = 0L;
        
        for (Object[] row : ratingCounts) {
            Integer rating = (Integer) row[0];
            Long count = ((Number) row[1]).longValue();
            
            switch (rating) {
                case 5: fiveStars = count; break;
                case 4: fourStars = count; break;
                case 3: threeStars = count; break;
                case 2: twoStars = count; break;
                case 1: oneStars = count; break;
            }
        }
        
        response.setRatingSummary(new ReviewListResponse.RatingSummary(
            fiveStars, fourStars, threeStars, twoStars, oneStars
        ));
        
        return response;
    }
    
    @Override
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.REVIEW_NOT_FOUND));
        
        return new ReviewResponse(review);
    }
    
    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.REVIEW_NOT_FOUND));
        
        // 작성자 본인 확인
        if (!review.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.REVIEW_UNAUTHORIZED_NOT_OWNER);
        }
        
        review.updateReview(request.getTitle(), request.getContent(), request.getRating());
        
        return new ReviewResponse(review);
    }
    
    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.REVIEW_NOT_FOUND));
        
        // 작성자 본인 확인
        if (!review.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.REVIEW_UNAUTHORIZED_NOT_OWNER);
        }
        
        reviewRepository.delete(review);
    }
    
    @Override
    public ReviewListResponse getMyReviews(Long memberId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        Page<ReviewResponse> reviewResponses = reviews.map(ReviewResponse::new);
        return new ReviewListResponse(reviewResponses);
    }
    
    @Override
    public boolean hasUserAttendedExpo(Long memberId, Long expoId) {
        return qrCodeRepository.existsByExpoIdAndMemberIdAndStatusUsed(expoId, memberId);
    }
    
    @Override
    public boolean hasUserReviewedExpo(Long memberId, Long expoId) {
        return reviewRepository.findByExpoIdAndMemberId(expoId, memberId) != null;
    }
    
    @Override
    public ReviewListResponse getBestReviews(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        Page<Review> reviews = reviewRepository.findBestReviews(pageable);
        Page<ReviewResponse> reviewResponses = reviews.map(ReviewResponse::new);
        return new ReviewListResponse(reviewResponses);
    }
}