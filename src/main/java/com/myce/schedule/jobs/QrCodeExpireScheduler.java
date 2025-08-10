package com.myce.schedule.jobs;

import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.schedule.TaskScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class QrCodeExpireScheduler implements TaskScheduler {

    private final QrCodeRepository qrCodeRepository;

    @Value("${scheduler.qr-code-expire:0 * * * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("QR 코드 만료 스케줄러가 등록되었습니다. cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.qr-code-expire:0 * * * * *}")
    @Transactional
    public void run() {
        try {
            process();
        } catch (Exception e) {
            log.error("QR 코드 만료 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Transactional
    public void process() {
        log.debug("QR 코드 만료 프로세스 시작");
        
        LocalDateTime now = LocalDateTime.now();
        log.info("만료 스케줄러 실행 - 현재 시간: {}", now);
        
        // Bulk Update로 성능 최적화
        int updatedCount = qrCodeRepository.bulkUpdateStatusToExpired(
                QrCodeStatus.ACTIVE, QrCodeStatus.EXPIRED, now);
        
        if (updatedCount > 0) {
            log.info("QR 코드 만료 처리 완료 - {} 개", updatedCount);
          
        } else {
            log.debug("만료 처리할 QR 코드가 없습니다.");
        }
        
        log.debug("QR 코드 만료 프로세스 완료");
    }
}