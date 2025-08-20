package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.*;
import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoCardResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.dto.TicketSummaryResponse;
import com.myce.expo.dto.BoothResponse;
import com.myce.expo.service.ExpoService;
import com.myce.expo.service.TicketService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos")
@RequiredArgsConstructor
public class ExpoController {

    private final ExpoService expoService;
    private final TicketService ticketService;

    // 박람회 등록
    @PostMapping
    public ResponseEntity<Long> saveExpo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                         @RequestBody @Valid ExpoRegistrationRequest expoRegistrationRequest) {
        Long memberId = customUserDetails.getMemberId();
        expoService.saveExpo(memberId, expoRegistrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    // 박람회 실시간 혼잡도 조회
    @GetMapping("/{expoId}/congestion")
    public ResponseEntity<CongestionResponse> getCongestionLevel(@PathVariable Long expoId) {
        CongestionResponse congestionResponse = expoService.getCongestionLevel(expoId);
        return ResponseEntity.ok(congestionResponse);
    }

    // 박람회 티켓 조회(예매용)
    @GetMapping("/{expoId}/tickets/reservations")
    public ResponseEntity<List<TicketSummaryResponse>> getTickets(@PathVariable Long expoId) {
        return ResponseEntity.ok(ticketService.getTickets(expoId));
    }

    // 박람회 카드 리스트 조회
    @GetMapping
    public ResponseEntity<Page<ExpoCardResponse>> getExpoCards(
        @RequestParam(required=false) String keyword,   // 검색
        @RequestParam(required=false) String category,  // 카테고리
        @RequestParam(required=false) String status,    // 박람회 상태 (PUBLISHED, PENDING_PUBLISH 등)
        @RequestParam(required=false) Integer period,   // 기간(1,3,6,12개월)
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,  // 사용자 지정 시작일
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,     // 사용자 지정 종료일
        Pageable pageable // 페이지네이션
    ) {
        Long memberId = getCurrentMemberIdOrNull();

        // 기간 from/to 자동 계산 (from/to 없을 때만)
        if (period != null && from == null && to == null) {
            int months = switch (period) { case 1,3,6,12 -> period; default -> 3; };
            LocalDate start = LocalDate.now(ZoneId.of("Asia/Seoul")); // Today
            LocalDate end = start.plusMonths(months); // Future date
            from = start;
            to   = end;
        }

        Page<ExpoCardResponse> expoCardsPage = expoService.getExpoCardsFiltered(memberId, category, status, from, to, keyword, pageable);
        return ResponseEntity.ok(expoCardsPage);
    }

    // 박람회 기본 정보 조회
    @GetMapping("/{expoId}/basic")
    public ResponseEntity<ExpoBasicResponse> getExpoBasicInfo(@PathVariable Long expoId) {
        ExpoBasicResponse basicInfo = expoService.getExpoBasicInfo(expoId);
        return ResponseEntity.ok(basicInfo);
    }

    // 박람회 찜하기 상태 조회
    @GetMapping("/{expoId}/bookmark")
    public ResponseEntity<ExpoBookmarkResponse> getExpoBookmarkStatus(@PathVariable Long expoId) {
        Long memberId = getCurrentMemberIdOrNull();
        ExpoBookmarkResponse bookmarkStatus = expoService.getExpoBookmarkStatus(expoId, memberId);
        return ResponseEntity.ok(bookmarkStatus);
    }

    // 박람회 리뷰 정보 조회 (비회원 접근 가능)
    @GetMapping("/{expoId}/reviews")
    public ResponseEntity<ExpoReviewsResponse> getExpoReviews(
            @PathVariable Long expoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ExpoReviewsResponse reviewsInfo = expoService.getExpoReviews(expoId, page, size);
        return ResponseEntity.ok(reviewsInfo);
    }

    // 박람회 위치 정보 조회
    @GetMapping("/{expoId}/location")
    public ResponseEntity<ExpoLocationResponse> getExpoLocation(@PathVariable Long expoId) {
        ExpoLocationResponse locationInfo = expoService.getExpoLocation(expoId);
        return ResponseEntity.ok(locationInfo);
    }

    // 박람회 부스 정보 조회 (공개용)
    @GetMapping("/{expoId}/booths/public")
    public ResponseEntity<List<BoothResponse>> getExpoBooths(@PathVariable Long expoId) {
        List<BoothResponse> booths = expoService.getExpoBooths(expoId);
        return ResponseEntity.ok(booths);
    }

    private Long getCurrentMemberIdOrNull(){
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            Object principal = auth.getPrincipal();
            if(principal instanceof CustomUserDetails user) {
                return user.getMemberId();
            }
            return null;
        } catch (Exception e) {
            // 비회원이거나 인증 관련 예외 발생시 null 반환
            return null;
        }
    }
}