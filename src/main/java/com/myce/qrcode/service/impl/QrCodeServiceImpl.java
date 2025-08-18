package com.myce.qrcode.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.service.S3Service;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.notification.service.NotificationService;
import com.myce.notification.service.SseService;
import com.myce.notification.service.SupportEmailService;
import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.qrcode.service.QrCodeService;
import com.myce.qrcode.service.mapper.QrResponseMapper;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReserverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final ReserverRepository reserverRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final S3Service s3Service;
    private final QrResponseMapper qrResponseMapper;
    private final SseService sseService;
    private final NotificationService notificationService;
    private final SupportEmailService supportEmailService;

    @Override
    @Transactional
    public void issueQr(Long reserverId) {
        log.info("QR 코드 발급 시작 - 예약자 ID: {}", reserverId);

        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_NOT_FOUND));

        if (qrCodeRepository.findByReserver(reserver).isPresent()) {
            log.debug("[issueQr] 중복 발급 요청 차단 - 이미 QR 존재. reserverId={}", reserverId);
            throw new CustomException(CustomErrorCode.QR_ALREADY_EXISTS);
        }

        try {
            createAndSaveQrCode(reserver);
            log.info("QR 코드 발급 완료 - 예약자 ID: {}", reserverId);
            
            // QR 발급 성공 알림 전송
            sendQrIssuedNotification(reserver, false);
        } catch (Exception e) {
            log.error("QR 코드 발급 실패 - 예약자 ID: {}, 오류: {}", reserverId, e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_GENERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void reissueQr(Long reserverId, Long adminMemberId, LoginType loginType) {
        log.info("QR 코드 재발급 시작 - 예약자 ID: {}, 관리자 ID: {}", reserverId, adminMemberId);

        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_NOT_FOUND));

        validateAdminPermission(adminMemberId, reserver, loginType);

        QrCode existing = qrCodeRepository.findByReserver(reserver)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        if (existing.getStatus() != QrCodeStatus.ACTIVE && existing.getStatus() != QrCodeStatus.APPROVED) {
            throw new CustomException(CustomErrorCode.QR_INVALID_STATUS);
        }

        try {
            log.info("기존 QR 코드 삭제 처리 - QR ID: {}", existing.getId());
            qrCodeRepository.delete(existing);
            qrCodeRepository.flush();

            createAndSaveQrCode(reserver);
            log.info("QR 코드 재발급 완료 - 예약자 ID: {}", reserverId);
            
            // QR 재발급 성공 알림 전송
            sendQrIssuedNotification(reserver, true);
        } catch (Exception e) {
            log.error("QR 코드 재발급 실패 - 예약자 ID: {}, 관리자 ID: {}, 오류: {}",
                    reserverId, adminMemberId, e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_REISSUE_FAILED);
        }

    }

    @Override
    @Transactional
    public QrUseResponse updateQrAsUsed(String qrToken, Long adminMemberId, LoginType loginType) {
        log.info("QR 코드 사용 처리 시작 - 토큰: {}, 관리자 ID: {}, 로그인 타입: {}", qrToken, adminMemberId, loginType);
        
        QrCode qr = qrCodeRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        validateAdminPermission(adminMemberId, qr.getReserver(), loginType);

        // ACTIVE인 경우만 상태 변경
        boolean wasActive = qr.getStatus() == QrCodeStatus.ACTIVE;
        if (wasActive) {
            qr.markAsUsed();
        }
        log.info("QR 코드 사용 처리 완료 - QR ID: {}, 예약자 ID: {}, 사용처리됨: {}", 
                qr.getId(), qr.getReserver().getId(), wasActive);

        // 매퍼를 통해 응답 생성 (사용 처리 성공/실패 구분)
        return qrResponseMapper.toUseResponse(qr, wasActive);
    }

    @Override
    @Transactional(readOnly = true)
    public String getQrImageUrlByReserverId(Long reserverId) {
        log.info("예약자 ID로 QR 이미지 URL 조회 - 예약자 ID: {}", reserverId);

        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_NOT_FOUND));

        QrCode qrCode = qrCodeRepository.findByReserver(reserver)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        String url = qrCode.getQrImageUrl();
        if (url == null) {
            throw new CustomException(CustomErrorCode.QR_NOT_FOUND);
        }

        return url;
    }

    @Override
    @Transactional(readOnly = true)
    public String getQrImageUrlByToken(String token) {
        log.info("토큰으로 QR 이미지 URL 조회 - 토큰: {}", token);

        QrCode qrCode = qrCodeRepository.findByQrToken(token)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        String url = qrCode.getQrImageUrl();
        if (url == null) {
            throw new CustomException(CustomErrorCode.QR_NOT_FOUND);
        }

        return url;
    }

    private void validateAdminPermission(Long adminId, Reserver reserver, LoginType loginType) {
        Long expoId = reserver.getReservation().getExpo().getId();
        
        switch (loginType) {
            case MEMBER -> {
                // Member 테이블의 PK로 전시회 소유자 확인
                Long managerId = reserver.getReservation()
                        .getExpo()
                        .getMember()
                        .getId();
                if (!managerId.equals(adminId)) {
                    throw new CustomException(CustomErrorCode.QR_UNAUTHORIZED);
                }
            }
            case ADMIN_CODE -> {
                // AdminCode 테이블의 PK로 전시회 권한 확인
                AdminCode adminCode = adminCodeRepository.findById(adminId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.ADMIN_CODE_NOT_FOUND));
                if (!adminCode.getExpoId().equals(expoId)) {
                    throw new CustomException(CustomErrorCode.QR_UNAUTHORIZED);
                }
            }
        }
    }

    private String uploadToStorage(byte[] image, String token) {
        try {
            return s3Service.uploadQrImage(image, token);
        } catch (Exception e) {
            log.error("QR 이미지 S3 업로드 실패 - token: {}, 오류: {}", token, e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_GENERATION_FAILED);
        }
    }

    private void createAndSaveQrCode(Reserver reserver) {
        try {
            String token = UUID.randomUUID().toString();

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(token, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            byte[] image = out.toByteArray();

            String imageUrl = uploadToStorage(image, token);
            
            // 티켓 정보를 통해 QR 코드 활성화/만료 시간 계산
            LocalDateTime activatedAt = calculateActivatedAt(reserver);
            LocalDateTime expiredAt = calculateExpiredAt(reserver);
            
            // 현재 시간이 티켓 사용 기간 내인지 확인하여 상태 결정
            LocalDateTime now = LocalDateTime.now();
            QrCodeStatus initialStatus = (now.isAfter(activatedAt) || now.isEqual(activatedAt)) && now.isBefore(expiredAt) 
                    ? QrCodeStatus.ACTIVE 
                    : QrCodeStatus.APPROVED;

            QrCode qr = QrCode.builder()
                    .reserver(reserver)
                    .qrToken(token)
                    .qrImageUrl(imageUrl)
                    .status(initialStatus)
                    .activatedAt(activatedAt)
                    .expiredAt(expiredAt)
                    .build();

            qrCodeRepository.save(qr);
        } catch (Exception e) {
            log.error("QR 코드 생성 중 오류 - 예약자 ID: {}, 오류: {}", reserver.getId(), e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_GENERATION_FAILED);
        }
    }

    /**
     * 티켓의 use_start_date 당일 00:00:00으로 활성화 시간 계산
     */
    private LocalDateTime calculateActivatedAt(Reserver reserver) {
        LocalDate ticketUseStartDate = reserver.getReservation().getTicket().getUseStartDate();
        return ticketUseStartDate.atStartOfDay();
    }

    /**
     * 티켓의 use_end_date 다음날 00:00:00으로 만료 시간 계산
     * (use_end_date 당일 종일 사용 가능하도록)
     */
    private LocalDateTime calculateExpiredAt(Reserver reserver) {
        LocalDate ticketUseEndDate = reserver.getReservation().getTicket().getUseEndDate();
        return ticketUseEndDate.plusDays(1).atStartOfDay();
    }

    @Override
    @Transactional(readOnly = true)
    public QrVerifyResponse verifyQrCode(String token, Long adminMemberId, LoginType loginType) {
        log.info("QR 코드 검증 시작 - token: {}, adminId: {}, 로그인 타입: {}", token, adminMemberId, loginType);
        
        QrCode qrCode = qrCodeRepository.findByQrToken(token)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        // 관리자 권한 검증
        validateAdminPermission(adminMemberId, qrCode.getReserver(), loginType);

        // 매퍼를 통해 응답 생성 (상태만 체크)
        QrVerifyResponse response = qrResponseMapper.toVerifyResponse(qrCode);
        
        log.info("QR 코드 검증 완료 - token: {}, 상태: {}, 유효성: {}", 
                token, qrCode.getStatus(), response.isValid());
        return response;
    }

    /**
     * QR 발급/재발급 시 알림 전송 (NotificationService에 위임)
     */
    private void sendQrIssuedNotification(Reserver reserver, boolean isReissue) {
        try {
            Reservation reservation = reserver.getReservation();
            
            String expoTitle = reservation.getExpo().getTitle();
            
            if (reservation.getUserType() == UserType.MEMBER) {
                // 회원: 사이트 내 알림 + SSE 전송
                Long memberId = reservation.getUserId();
                notificationService.sendQrIssuedNotification(memberId, reservation.getId(), expoTitle, isReissue);
                log.info("회원 QR {} 알림 처리 완료 - 예약자 ID: {}, 회원 ID: {}", 
                        isReissue ? "재발급" : "발급", reserver.getId(), memberId);
            } else {
                // 비회원: 이메일 알림
                String subject = String.format("[박람회 QR 코드 %s] %s", 
                        isReissue ? "재발급" : "발급", expoTitle);
                String body = String.format(
                    "안녕하세요 %s님,<br><br>" +
                        "박람회 '%s'의 QR 코드가 %s되었습니다.<br><br>" +
                        "[예매 정보]<br>" +
                        "- 예약자: %s<br>" +
                        "- 예약번호: %s<br>" +
                        "QR 코드는 박람회 당일 입장 시 필요합니다.<br>" +
                        "예매 상세 조회: <a href=\"https://myce.live/guest-reservation\">바로가기</a><br><br>" +
                        "감사합니다.",
                    reserver.getName(),
                    expoTitle,
                    isReissue ? "재발급" : "발급",
                    reserver.getName(),
                    reservation.getReservationCode()
                );
                
                supportEmailService.sendSupportMail(reserver.getEmail(), subject, body);
                log.info("비회원 QR {} 이메일 알림 전송 완료 - 예약자 ID: {}, 이메일: {}", 
                        isReissue ? "재발급" : "발급", reserver.getId(), reserver.getEmail());
            }
        } catch (Exception e) {
            log.error("QR 발급 알림 처리 실패 - 예약자 ID: {}, 오류: {}", 
                    reserver.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void issueQrForReservation(Long reservationId) {
        log.info("예매 완료 시 QR 코드 즉시 생성 시작 - 예약 ID: {}", reservationId);
        
        // 해당 예약의 모든 예약자 조회
        List<Reserver> reservers = reserverRepository.findByReservationId(reservationId);
        
        if (reservers.isEmpty()) {
            log.warn("예약자를 찾을 수 없습니다 - 예약 ID: {}", reservationId);
            return;
        }
        
        Reservation reservation = reservers.get(0).getReservation();
        Expo expo = reservation.getExpo();
        LocalDate today = LocalDate.now();
        LocalDate expoStartDate = expo.getStartDate();
        LocalDate twoDaysBefore = expoStartDate.minusDays(2);
        
        // 박람회 시작 2일 전부터 즉시 QR 생성 (스케줄러 백업)
        if (today.isAfter(twoDaysBefore) || today.isEqual(twoDaysBefore)) {
            log.info("박람회 시작 2일 전 이후 예매 감지 - 즉시 QR 생성. 박람회: {}, 시작일: {}, 오늘: {}", 
                    expo.getTitle(), expoStartDate, today);
            
            int successCount = 0;
            int failCount = 0;
            
            for (Reserver reserver : reservers) {
                try {
                    // 기존 QR이 있는지 확인
                    if (qrCodeRepository.findByReserver(reserver).isPresent()) {
                        log.debug("이미 QR 코드가 존재함 - 예약자 ID: {}", reserver.getId());
                        continue;
                    }
                    
                    // 날짜에 따라 적절한 상태로 QR 코드 생성
                    createQrCodeWithAppropriateStatus(reserver);
                    log.debug("즉시 QR 코드 생성 완료 - 예약자 ID: {}", reserver.getId());
                    
                    // 알림 전송
                    sendQrIssuedNotification(reserver, false);
                    successCount++;
                } catch (Exception e) {
                    log.error("즉시 QR 코드 생성 실패 - 예약자 ID: {}, 오류: {}", 
                            reserver.getId(), e.getMessage(), e);
                    failCount++;
                }
            }
            
            log.info("예매 완료 시 QR 코드 즉시 생성 완료 - 예약 ID: {}, 성공: {} 명, 실패: {} 명", 
                    reservationId, successCount, failCount);
        } else {
            log.info("박람회 시작까지 2일 이상 남음 - 스케줄러에서 생성 예정. 박람회: {}, 시작일: {}, 오늘: {}", 
                    expo.getTitle(), expoStartDate, today);
        }
    }
    
    /**
     * 현재 날짜에 따라 적절한 상태로 QR 코드 생성
     */
    private void createQrCodeWithAppropriateStatus(Reserver reserver) {
        try {
            String token = UUID.randomUUID().toString();
            
            // QR 이미지 생성
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(token, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            byte[] image = out.toByteArray();
            
            String imageUrl = uploadToStorage(image, token);
            
            // 티켓 정보를 통해 QR 코드 활성화/만료 시간 계산
            LocalDateTime activatedAt = calculateActivatedAt(reserver);
            LocalDateTime expiredAt = calculateExpiredAt(reserver);
            
            // 현재 시간이 티켓 사용 기간 내인지 확인하여 상태 결정
            LocalDateTime now = LocalDateTime.now();
            QrCodeStatus status = (now.isAfter(activatedAt) || now.isEqual(activatedAt)) && now.isBefore(expiredAt) 
                    ? QrCodeStatus.ACTIVE 
                    : QrCodeStatus.APPROVED;
            
            QrCode qr = QrCode.builder()
                    .reserver(reserver)
                    .qrToken(token)
                    .qrImageUrl(imageUrl)
                    .status(status)
                    .activatedAt(activatedAt)
                    .expiredAt(expiredAt)
                    .build();
            
            qrCodeRepository.save(qr);
            
            log.info("날짜 기반 QR 코드 생성 완료 - 예약자 ID: {}, 상태: {}", reserver.getId(), status);
        } catch (Exception e) {
            log.error("날짜 기반 QR 코드 생성 실패 - 예약자 ID: {}, 오류: {}", reserver.getId(), e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_GENERATION_FAILED);
        }
    }
}
