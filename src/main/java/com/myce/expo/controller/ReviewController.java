package com.myce.expo.controller;

import com.myce.expo.dto.*;
import com.myce.expo.service.ReviewService;
import com.myce.auth.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        ReviewResponse response = reviewService.createReview(request, userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/expo/{expoId}")
    public ResponseEntity<ReviewListResponse> getReviewsByExpo(
            @PathVariable Long expoId,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        ReviewListResponse response = reviewService.getReviewsByExpo(expoId, sortBy, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        ReviewResponse response = reviewService.updateReview(reviewId, request, userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        reviewService.deleteReview(reviewId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my")
    public ResponseEntity<ReviewListResponse> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        ReviewListResponse response = reviewService.getMyReviews(userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-attendance/{expoId}")
    public ResponseEntity<Boolean> checkAttendance(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        boolean hasAttended = reviewService.hasUserAttendedExpo(userDetails.getMemberId(), expoId);
        return ResponseEntity.ok(hasAttended);
    }
    
    @GetMapping("/check-reviewed/{expoId}")
    public ResponseEntity<Boolean> checkReviewed(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        boolean hasReviewed = reviewService.hasUserReviewedExpo(userDetails.getMemberId(), expoId);
        return ResponseEntity.ok(hasReviewed);
    }
    
    @GetMapping("/best")
    public ResponseEntity<ReviewListResponse> getBestReviews(
            @RequestParam(defaultValue = "6") int limit) {
        
        ReviewListResponse response = reviewService.getBestReviews(limit);
        return ResponseEntity.ok(response);
    }
}