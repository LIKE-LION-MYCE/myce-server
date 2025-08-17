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
        
        // 박람회 참석 여부 확인 (임시로 주석 처리 - 테스트용)
        /*
        if (!hasUserAttendedExpo(memberId, request.getExpoId())) {
            throw new CustomException(CustomErrorCode.REVIEW_UNAUTHORIZED_NOT_ATTENDED);
        }
        */
        
        // 이미 리뷰를 작성했는지 확인 (임시로 주석 처리 - 테스트용)
        /*
        if (hasUserReviewedExpo(memberId, request.getExpoId())) {
            throw new CustomException(CustomErrorCode.REVIEW_ALREADY_EXISTS);
        }
        */
        
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
        return new ReviewListResponse(reviewResponses);
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
        // 임시로 로그인한 사용자는 누구나 리뷰 작성 가능
        return true;
        
        // 실제 운영시 사용할 코드 (위의 return true를 주석처리하고 아래 주석 해제)
        // return qrCodeRepository.existsByExpoIdAndMemberIdAndStatusUsed(expoId, memberId);
    }
    
    @Override
    public boolean hasUserReviewedExpo(Long memberId, Long expoId) {
        return reviewRepository.findByExpoIdAndMemberId(expoId, memberId) != null;
    }
}