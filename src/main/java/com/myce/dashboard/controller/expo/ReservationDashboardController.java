package com.myce.dashboard.controller.expo;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.dashboard.dto.expo.ExpoDashboardResponse;
import com.myce.dashboard.dto.expo.DailyReservation;
import com.myce.dashboard.service.expo.ExpoDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/dashboard")
@RequiredArgsConstructor
public class ReservationDashboardController {
    
    private final ExpoDashboardService expoDashboardService;
    
    @GetMapping
    public ResponseEntity<ExpoDashboardResponse> getExpoDashboard(@PathVariable Long expoId) {
        ExpoDashboardResponse response = expoDashboardService.getExpoDashboard(expoId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cache/refresh/reservation")
    public ResponseEntity<String> refreshReservationCache(@PathVariable Long expoId) {
        expoDashboardService.refreshReservationCache(expoId);
        return ResponseEntity.ok("예약 통계 캐시가 갱신되었습니다.");
    }
    
    @PostMapping("/cache/refresh/checkin")
    public ResponseEntity<String> refreshCheckinCache(@PathVariable Long expoId) {
        expoDashboardService.refreshCheckinCache(expoId);
        return ResponseEntity.ok("체크인 통계 캐시가 갱신되었습니다.");
    }
    
    @PostMapping("/cache/refresh/payment")
    public ResponseEntity<String> refreshPaymentCache(@PathVariable Long expoId) {
        expoDashboardService.refreshPaymentCache(expoId);
        return ResponseEntity.ok("결제 통계 캐시가 갱신되었습니다.");
    }
    
    @PostMapping("/cache/refresh/all")
    public ResponseEntity<String> refreshAllCache(@PathVariable Long expoId) {
        expoDashboardService.refreshReservationCache(expoId);
        expoDashboardService.refreshCheckinCache(expoId);
        expoDashboardService.refreshPaymentCache(expoId);
        return ResponseEntity.ok("모든 통계 캐시가 갱신되었습니다.");
    }
    
    @GetMapping("/expo-date-range")
    public ResponseEntity<LocalDate[]> getExpoDisplayDateRange(@PathVariable Long expoId) {
        LocalDate[] dateRange = expoDashboardService.getExpoDisplayDateRange(expoId);
        return ResponseEntity.ok(dateRange);
    }
    
    @GetMapping("/reservations/weekly")
    public ResponseEntity<List<DailyReservation>> getWeeklyReservationsByDateRange(
            @PathVariable Long expoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<DailyReservation> reservations = expoDashboardService.getWeeklyReservationsByDateRange(expoId, startDate, endDate);
        return ResponseEntity.ok(reservations);
    }
}
