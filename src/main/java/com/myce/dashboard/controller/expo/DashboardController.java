package com.myce.dashboard.controller.expo;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.dashboard.dto.expo.ExpoDashboardResponse;
import com.myce.dashboard.dto.expo.DailyReservation;
import com.myce.dashboard.dto.expo.WeeklyReservationResponse;
import com.myce.dashboard.dto.expo.HourlyCheckin;
import com.myce.dashboard.service.expo.ExpoDashboardService;
import com.myce.dashboard.service.expo.CheckinStatsService;
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
public class DashboardController {

    private final ExpoDashboardService expoDashboardService;
    private final CheckinStatsService checkinStatsService;
    private final ExpoAdminAccessValidate expoAdminAccessValidate;

    @GetMapping
    public ResponseEntity<ExpoDashboardResponse> getExpoDashboard(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        expoAdminAccessValidate.ensureAdmin(expoId, userDetails.getMemberId(), userDetails.getLoginType());
        ExpoDashboardResponse response = expoDashboardService.getExpoDashboard(expoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/refresh")
    public ResponseEntity<String> refreshAllCache(@PathVariable Long expoId) {
        expoDashboardService.refreshReservationCache(expoId);
        expoDashboardService.refreshCheckinCache(expoId);
        expoDashboardService.refreshPaymentCache(expoId);
        return ResponseEntity.ok("대시보드 통계 캐시가 갱신되었습니다.");
    }
    
    @DeleteMapping("/cache/clear")
    public ResponseEntity<String> clearAllCache(@PathVariable Long expoId) {
        expoDashboardService.clearReservationCache(expoId);
        expoDashboardService.clearCheckinCache(expoId);
        expoDashboardService.clearPaymentCache(expoId);
        return ResponseEntity.ok("대시보드 통계 캐시가 완전히 삭제되었습니다.");
    }

    @GetMapping("/expo-date-range")
    public ResponseEntity<LocalDate[]> getExpoDisplayDateRange(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        expoAdminAccessValidate.ensureAdmin(expoId, userDetails.getMemberId(), userDetails.getLoginType());
        LocalDate[] dateRange = expoDashboardService.getExpoDisplayDateRange(expoId);
        return ResponseEntity.ok(dateRange);
    }

    @GetMapping("/reservations/weekly")
    public ResponseEntity<WeeklyReservationResponse> getWeeklyReservationsByDateRange(
            @PathVariable Long expoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        expoAdminAccessValidate.ensureAdmin(expoId, userDetails.getMemberId(), userDetails.getLoginType());
        List<DailyReservation> reservations = expoDashboardService.getWeeklyReservationsByDateRange(expoId, startDate, endDate);
        LocalDate[] displayDateRange = expoDashboardService.getExpoDisplayDateRange(expoId);
        
        WeeklyReservationResponse response = WeeklyReservationResponse.builder()
                .expoDisplayDateRange(displayDateRange)
                .dailyReservations(reservations)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkins/hourly")
    public ResponseEntity<List<HourlyCheckin>> getHourlyCheckinsByDate(
            @PathVariable Long expoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        expoAdminAccessValidate.ensureAdmin(expoId, userDetails.getMemberId(), userDetails.getLoginType());
        List<HourlyCheckin> hourlyCheckins = checkinStatsService.getHourlyCheckinsByDate(expoId, date);
        return ResponseEntity.ok(hourlyCheckins);
    }
}
