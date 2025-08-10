package com.myce.qrcode.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.service.S3Service;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.qrcode.service.QrCodeService;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.repository.ReserverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final ReserverRepository reserverRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void issueQr(Long reserverId) {
        log.info("QR 코드 발급 시작 - 예약자 ID: {}", reserverId);

        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_NOT_FOUND));

        if (qrCodeRepository.findByReserver(reserver).isPresent()) {
            throw new CustomException(CustomErrorCode.QR_ALREADY_EXISTS);
        }

        try {
            createAndSaveQrCode(reserver);
            log.info("QR 코드 발급 완료 - 예약자 ID: {}", reserverId);
        } catch (Exception e) {
            log.error("QR 코드 발급 실패 - 예약자 ID: {}, 오류: {}", reserverId, e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_GENERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void reissueQr(Long reserverId, Long adminMemberId) {
        log.info("QR 코드 재발급 시작 - 예약자 ID: {}, 관리자 ID: {}", reserverId, adminMemberId);

        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_NOT_FOUND));

        validateExpoManager(adminMemberId, reserver);

        QrCode existing = qrCodeRepository.findByReserver(reserver)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        if (existing.getStatus() != QrCodeStatus.ACTIVE) {
            throw new CustomException(CustomErrorCode.QR_INVALID_STATUS);
        }

        try {
            log.info("기존 QR 코드 삭제 처리 - QR ID: {}", existing.getId());
            qrCodeRepository.delete(existing);
            qrCodeRepository.flush();

            createAndSaveQrCode(reserver);
            log.info("QR 코드 재발급 완료 - 예약자 ID: {}", reserverId);
        } catch (Exception e) {
            log.error("QR 코드 재발급 실패 - 예약자 ID: {}, 관리자 ID: {}, 오류: {}",
                    reserverId, adminMemberId, e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_REISSUE_FAILED);
        }

    }

    @Override
    @Transactional
    public void markQrAsUsed(String qrToken, Long adminMemberId) {
        log.info("QR 코드 사용 처리 시작 - 토큰: {}, 관리자 ID: {}", qrToken, adminMemberId);
        
        QrCode qr = qrCodeRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        validateExpoManager(adminMemberId, qr.getReserver());

        if (qr.getStatus() == QrCodeStatus.USED) {
            throw new CustomException(CustomErrorCode.QR_ALREADY_USED);
        }
        
        if (qr.getStatus() == QrCodeStatus.EXPIRED) {
            throw new CustomException(CustomErrorCode.QR_EXPIRED);
        }

        qr.markAsUsed();
        log.info("QR 코드 사용 처리 완료 - QR ID: {}, 예약자 ID: {}", 
                qr.getId(), qr.getReserver().getId());
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

    private void validateExpoManager(Long adminId, Reserver reserver) {
        Long managerId = reserver.getReservation()
                .getExpo()
                .getMember()
                .getId();

        if (!managerId.equals(adminId)) {
            throw new CustomException(CustomErrorCode.QR_UNAUTHORIZED);
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

            QrCode qr = QrCode.builder()
                    .reserver(reserver)
                    .qrToken(token)
                    .qrImageUrl(imageUrl)
                    .status(QrCodeStatus.APPROVED)
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
     * 티켓의 use_end_date 당일 23:59:59로 만료 시간 계산
     */
    private LocalDateTime calculateExpiredAt(Reserver reserver) {
        LocalDate ticketUseEndDate = reserver.getReservation().getTicket().getUseEndDate();
        return ticketUseEndDate.atTime(23, 59, 59);
    }
}
