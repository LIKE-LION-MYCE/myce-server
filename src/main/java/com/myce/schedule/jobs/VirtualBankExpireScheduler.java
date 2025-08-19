package com.myce.schedule.jobs;

import com.myce.reservation.service.VirtualBankExpireService;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VirtualBankExpireScheduler implements TaskScheduler {
    
    private final VirtualBankExpireService virtualBankExpireService;

    @PostConstruct
    public void init() {
        log.info("[Scheduler] 가상계좌 만료 처리 스케줄러 등록 완료");
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void run() {
        try {
            log.info("[VirtualBankExpireScheduler] 가상계좌 만료 처리 시작");
            this.process();
            log.info("[VirtualBankExpireScheduler] 가상계좌 만료 처리 완료");
        } catch (Exception e) {
            log.error("[VirtualBankExpireScheduler] 가상계좌 만료 처리 실패", e);
        }
    }

    @Override
    public void process() {
        virtualBankExpireService.processExpiredVirtualBankReservations();
    }
}