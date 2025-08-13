package com.myce.schedule.jobs;

import com.myce.expo.service.SystemExpoService;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoScheduler implements TaskScheduler {
    
    private final SystemExpoService systemExpoService;

    @Value("${scheduler.expo-publish:0 */10 * * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("박람회 게시 상태 관리 스케줄러가 등록되었습니다. cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.expo-publish:0 */10 * * * *}")
    public void run() {
        try {
            this.process();
        } catch (Exception e) {
            log.error("박람회 게시 상태 관리 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Override
    @Transactional
    public void process() {
        log.debug("박람회 게시 상태 관리 프로세스 시작");
        
        int published = systemExpoService.publishPendingExpos();
        int completed = systemExpoService.closeCompletedExpos();

        if (published > 0 || completed > 0) {
            systemExpoService.refreshExpoCache();
            log.info("박람회 상태 업데이트 완료 - 게시: {}개, 종료: {}개", published, completed);
        } else {
            log.debug("상태 변경할 박람회가 없습니다.");
        }
        
        log.debug("박람회 게시 상태 관리 프로세스 완료");
    }
}