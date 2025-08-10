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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QrCodeActivateScheduler implements TaskScheduler {

    private final QrCodeRepository qrCodeRepository;

    @Value("${scheduler.qr-code-activate:0 * * * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("QR 코드 활성화 스케줄러가 등록되었습니다. cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.qr-code-activate:0 * * * * *}")
    public void run() {
        try {
            process();
        } catch (Exception e) {
            log.error("QR 코드 활성화 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Transactional
    public void process() {
        log.debug("QR 코드 활성화 프로세스 시작");

        LocalDateTime now = LocalDateTime.now();
        log.info("스케줄러 실행 - 현재 시간: {}", now);

        List<QrCode> qrCodesToActivate = qrCodeRepository.findByStatusAndActivatedAtBefore(
                QrCodeStatus.APPROVED, now);

        log.info("활성화 대상 QR코드 수: {}", qrCodesToActivate.size());
        // ...
        
        if (!qrCodesToActivate.isEmpty()) {
            qrCodesToActivate.forEach(qrCode -> {
                log.info("QR코드 ID: {}, 변경 전 상태: {}", qrCode.getId(), qrCode.getStatus());
                qrCode.activate();
                log.info("QR코드 ID: {}, 변경 후 상태: {}", qrCode.getId(), qrCode.getStatus());
            });
            qrCodeRepository.saveAll(qrCodesToActivate);
            log.info("QR 코드 활성화 완료 - {} 개", qrCodesToActivate.size());
        } else {
            log.debug("활성화할 QR 코드가 없습니다.");
        }
        
        log.debug("QR 코드 활성화 프로세스 완료");
    }
}