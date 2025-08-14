package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.*;
import com.myce.expo.dto.TicketSummaryResponse;
import com.myce.expo.dto.BoothResponse;
import com.myce.expo.entity.Ticket;
import com.myce.expo.service.ExpoService;
import com.myce.expo.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos")
@RequiredArgsConstructor
public class ExpoController {
    private final ExpoService exposervice;
    private final TicketService ticketService;

    // 박람회 등록
    @PostMapping
    public ResponseEntity<Long> saveExpo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                         @RequestBody @Valid ExpoRegistrationRequest expoRegistrationRequest) {
        Long memberId = customUserDetails.getMemberId();
        exposervice.saveExpo(memberId, expoRegistrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    // 박람회 실시간 혼잡도 조회
    @GetMapping("/{expoId}/congestion")
    public ResponseEntity<CongestionResponse> getCongestionLevel(@PathVariable Long expoId) {
        CongestionResponse congestionResponse = exposervice.getCongestionLevel(expoId);
        return ResponseEntity.ok(congestionResponse);
    }

    // 박람회 티켓 조회(예매용)
    @GetMapping("/{expoId}/tickets/reservations")
    public ResponseEntity<List<TicketSummaryResponse>> getTickets(@PathVariable Long expoId) {
        return ResponseEntity.ok(ticketService.getTickets(expoId));
    }
    
    // 박람회 기본 정보 조회
    @GetMapping("/{expoId}/basic")
    public ResponseEntity<ExpoBasicResponse> getExpoBasicInfo(@PathVariable Long expoId) {
        ExpoBasicResponse basicInfo = exposervice.getExpoBasicInfo(expoId);
        return ResponseEntity.ok(basicInfo);
    }
    // 박람회 찜하기 상태 조회
    @GetMapping("/{expoId}/bookmark")
    public ResponseEntity<ExpoBookmarkResponse> getExpoBookmarkStatus(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails != null ? customUserDetails.getMemberId() : null;
        ExpoBookmarkResponse bookmarkStatus = exposervice.getExpoBookmarkStatus(expoId, memberId);
        return ResponseEntity.ok(bookmarkStatus);
    }
    
    // 박람회 리뷰 정보 조회
    @GetMapping("/{expoId}/reviews")
    public ResponseEntity<ExpoReviewsResponse> getExpoReviews(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = customUserDetails != null ? customUserDetails.getMemberId() : null;
        ExpoReviewsResponse reviewsInfo = exposervice.getExpoReviews(expoId, memberId, page, size);
        return ResponseEntity.ok(reviewsInfo);
    }
    
    // 박람회 위치 정보 조회
    @GetMapping("/{expoId}/location")
    public ResponseEntity<ExpoLocationResponse> getExpoLocation(@PathVariable Long expoId) {
        ExpoLocationResponse locationInfo = exposervice.getExpoLocation(expoId);
        return ResponseEntity.ok(locationInfo);
    }
    
    // 박람회 부스 정보 조회 (공개용)
    @GetMapping("/{expoId}/booths/public")
    public ResponseEntity<List<BoothResponse>> getExpoBooths(@PathVariable Long expoId) {
        List<BoothResponse> booths = exposervice.getExpoBooths(expoId);
        return ResponseEntity.ok(booths);
    }
}
