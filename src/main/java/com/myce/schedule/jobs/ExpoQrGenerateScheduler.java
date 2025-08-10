package com.myce.schedule.jobs;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.qrcode.service.QrCodeService;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.schedule.TaskScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpoQrGenerateScheduler implements TaskScheduler {

    private final ExpoRepository expoRepository;
    private final ReserverRepository reserverRepository;
    private final QrCodeService qrCodeService;

    @Value("${scheduler.expo-qr-generate:0 0 0 * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("박람회 QR코드 일괄 생성 스케줄러가 등록되었습니다. cron: {}", cronExpression);
    }

    @Override
    @Transactional
    @Scheduled(cron = "${scheduler.expo-qr-generate:0 0 0 * * *}")
    public void run() {
        try {
            process();
        } catch (Exception e) {
            log.error("박람회 QR코드 일괄 생성 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Transactional
    public void process() {
        log.info("박람회 QR코드 일괄 생성 프로세스 시작");
        
        // 이틀 후 시작하는 게시된 박람회들 조회
        LocalDate twoDaysLater = LocalDate.now().plusDays(2);
        
        List<Expo> targetExpos = expoRepository.findByStartDateAndStatus(twoDaysLater, ExpoStatus.PUBLISHED);
        
        log.info("QR코드 생성 대상 박람회 수: {} 개 ({})", targetExpos.size(), twoDaysLater);
        
        for (Expo expo : targetExpos) {
            generateQrCodesForExpo(expo);
        }
        
        log.info("박람회 QR코드 일괄 생성 프로세스 완료");
    }

    private void generateQrCodesForExpo(Expo expo) {
        log.info("박람회 QR코드 생성 시작 - 박람회: {} (ID: {})", expo.getTitle(), expo.getId());
        
        // QR코드가 없는 예약자들만 조회
        List<Reserver> reserversWithoutQr = reserverRepository.findReserversWithoutQrCodeByExpo(expo.getId());
        
        if (reserversWithoutQr.isEmpty()) {
            log.info("QR코드 생성 대상이 없습니다 - 박람회: {}", expo.getTitle());
            return;
        }
        
        log.info("QR코드 생성 시작 - 박람회: {}, 대상 예약자 수: {} 명", 
                expo.getTitle(), reserversWithoutQr.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Reserver reserver : reserversWithoutQr) {
            try {
                qrCodeService.issueQr(reserver.getId());
                successCount++;
            } catch (Exception e) {
                log.error("QR코드 생성 실패 - 예약자 ID: {}, 오류: {}", 
                        reserver.getId(), e.getMessage());
                failCount++;
            }
        }
        
        log.info("박람회 QR코드 생성 완료 - 박람회: {}, 성공: {} 명, 실패: {} 명", 
                expo.getTitle(), successCount, failCount);
    }
}