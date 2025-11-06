package com.myce.schedule.jobs;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.notification.service.NotificationService;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.qrcode.service.QrCodeService;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpoQrGenerateScheduler implements TaskScheduler {

    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final QrCodeService qrCodeService;
    private final QrCodeRepository qrCodeRepository;
    private final NotificationService notificationService;

    @Value("${scheduler.expo-qr-generate:0 0 0 * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("박람회 QR코드 일괄 생성 스케줄러가 등록되었습니다. cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.expo-qr-generate:0 0 0 * * *}")
    public void run() {
        try {
            process();
        } catch (Exception e) {
            log.error("박람회 QR코드 일괄 생성 스케줄러 실행 중 오류 발생", e);
        }
    }

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

        List<Reserver> reservers = reserverRepository.findReserversByExpo(expo.getId());
        
        if (reservers.isEmpty()) {
            log.info("QR코드 생성 대상이 없습니다 - 박람회: {}", expo.getTitle());
            return;
        }
        
        log.info("QR코드 생성 시작 - 박람회: {}, 대상 예약자 수: {} 명", 
                expo.getTitle(), reservers.size());
        
        int successCount = 0;
        int failCount = 0;
        
        // QR 생성이 성공한 예약 ID들을 수집
        Set<Long> processedReservations = new HashSet<>();
        
        // 1단계: 모든 reserver에 대해 QR 생성
        for (Reserver reserver : reservers) {
            try {
                // 이미 QR이 있는지 먼저 확인
                if (qrCodeRepository.findByReserver(reserver).isPresent()) {
                    log.debug("QR코드 이미 존재 - 예약자 ID: {}", reserver.getId());
                    continue; // 이미 있으면 건너뛰고 알림도 보내지 않음
                }
                
                // QR 생성 (알림 없음)
                qrCodeService.issueQrWithoutNotification(reserver.getId());
                log.debug("QR코드 발급 완료 - 예약자 ID: {}", reserver.getId());
                
                // QR 생성 성공한 예약 ID 수집
                processedReservations.add(reserver.getReservation().getId());
                successCount++;
            } catch (Exception e) {
                log.error("QR코드 생성 실패 - 예약자 ID: {}, 오류: {}", reserver.getId(), e.getMessage());
                failCount++;
            }
        }
        
        // 2단계: QR 생성이 성공한 예약들에 대해 알림 전송
        int notificationCount = 0;
        for (Long reservationId : processedReservations) {
            try {
                notificationService.sendQrIssuedNotificationByReservationId(reservationId);
                notificationCount++;
                log.debug("QR 발급 알림 전송 완료 - 예약 ID: {}", reservationId);
            } catch (Exception e) {
                log.error("QR 발급 알림 전송 실패 - 예약 ID: {}, 오류: {}", 
                        reservationId, e.getMessage());
            }
        }
        
        log.info("박람회 QR코드 생성 완료 - 박람회: {}, 성공: {} 명, 실패: {} 명, 알림 전송: {} 건", 
                expo.getTitle(), successCount, failCount, notificationCount);
    }

}