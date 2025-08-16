package com.myce.dashboard.service.expo;

import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoStatsScheduler {

    private final ExpoDashboardService expoDashboardService;
    private final ExpoRepository expoRepository;

    /**
     * 5분 주기로 실시간성이 중요한 캐시 갱신
     * - 오늘 시간대별 입장인원
     * - 날짜별 예약자 수 (일주일)
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300000ms
    public void refreshRealtimeStats() {
        log.info("=== 5분 주기 실시간 통계 갱신 시작 ===");

        try {
            List<Long> activeExpoIds = getActiveExpoIds();
            log.info("활성 박람회 {} 개에 대한 실시간 통계 갱신", activeExpoIds.size());

            for (Long expoId : activeExpoIds) {
                try {
                    expoDashboardService.refreshReservationCache(expoId);
                    expoDashboardService.refreshCheckinCache(expoId);
                } catch (Exception e) {
                    log.error("박람회 ID {} 실시간 통계 갱신 실패", expoId, e);
                }
            }
            log.info("5분 주기 실시간 통계 갱신 완료");
        } catch (Exception e) {
            log.error("5분 주기 실시간 통계 갱신 실패", e);
        }
    }

    /**
     * 10분 주기로 결제 관련 캐시 갱신
     * - 결제 대기 건수
     * - 오늘 총 수익
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600000ms
    public void refreshPaymentStats() {
        log.info("=== 10분 주기 결제 통계 갱신 시작 ===");

        try {
            List<Long> activeExpoIds = getActiveExpoIds();
            log.info("활성 박람회 {} 개에 대한 결제 통계 갱신", activeExpoIds.size());

            for (Long expoId : activeExpoIds) {
                try {
                    expoDashboardService.refreshPaymentCache(expoId);
                } catch (Exception e) {
                    log.error("박람회 ID {} 결제 통계 갱신 실패", expoId, e);
                }
            }
            log.info("10분 주기 결제 통계 갱신 완료");
        } catch (Exception e) {
            log.error("10분 주기 결제 통계 갱신 실패", e);
        }
    }

    /**
     * 매일 새벽 2시에 누적 통계 데이터 갱신
     * - 누적 예약자수
     * - 예약자 성별/연령대별 통계
     */
    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    public void refreshDailyStats() {
        log.info("=== 새벽 배치 누적 통계 갱신 시작 ===");

        try {
            // 복잡한 집계 쿼리는 새벽에 처리하여 부하 분산
            refreshAccumulatedStats();
            log.info("새벽 배치 누적 통계 갱신 완료");
        } catch (Exception e) {
            log.error("새벽 배치 누적 통계 갱신 실패", e);
        }
    }

    /**
     * 정산 완료 후 수동 호출용 메서드
     * - 결제 완료 건수
     * - 취소 및 환불 건수
     * - 총 수익 합계
     * - 티켓 종류별 판매 현황
     */
    public void refreshSettlementStats() {
        log.info("=== 정산 완료 후 통계 갱신 시작 ===");

        try {
            // 정산 검증 완료 후 확정 데이터 반영
            refreshVerifiedPaymentStats();
            log.info("정산 완료 후 통계 갱신 완료");
        } catch (Exception e) {
            log.error("정산 완료 후 통계 갱신 실패", e);
        }
    }

    /**
     * 매일 00:01에 당일 초기화 및 캐시 워밍업
     */
    @Scheduled(cron = "0 1 0 * * ?") // 매일 00:01
    public void dailyInitialization() {
        log.info("=== 일일 초기화 및 캐시 워밍업 시작 ===");

        try {
            // 당일 통계 초기화
            initializeDailyStats();

            // 캐시 워밍업
            List<Long> activeExpoIds = getActiveExpoIds();
            log.info("활성 박람회 {} 개에 대한 캐시 워밍업", activeExpoIds.size());

            for (Long expoId : activeExpoIds) {
                try {
                    expoDashboardService.refreshReservationCache(expoId);
                    expoDashboardService.refreshCheckinCache(expoId);
                    expoDashboardService.refreshPaymentCache(expoId);
                } catch (Exception e) {
                    log.error("박람회 ID {} 캐시 워밍업 실패", expoId, e);
                }
            }

            log.info("일일 초기화 및 캐시 워밍업 완료");
        } catch (Exception e) {
            log.error("일일 초기화 및 캐시 워밍업 실패", e);
        }
    }

    private void refreshAccumulatedStats() {
        // 실제 구현에서는 복잡한 집계 쿼리를 통해 누적 통계 계산
        log.info("누적 예약자수 및 성별/연령대별 통계 갱신");
        // TODO: Repository를 통한 실제 DB 조회 및 집계 로직 구현
    }

    private void refreshVerifiedPaymentStats() {
        // 실제 구현에서는 정산 검증 완료된 데이터만 조회하여 통계 갱신
        log.info("검증된 결제 통계 데이터 갱신");
        // TODO: 정산 상태 확인 후 확정 데이터 반영 로직 구현
    }

    private void initializeDailyStats() {
        // 당일 통계 초기화 로직
        log.info("당일 통계 초기값 설정");
        // TODO: Redis에 당일 초기값 설정 로직 구현
    }

    /**
     * 활성 상태인 박람회 ID 목록 조회
     * PUBLISHED 상태의 박람회들
     */
    private List<Long> getActiveExpoIds() {
        return expoRepository.findIdsByStatus(ExpoStatus.PUBLISHED);
    }
}