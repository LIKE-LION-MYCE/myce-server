package com.myce.qrcode.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.service.S3Service;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.qrcode.service.QrCodeGenerateService;
import com.myce.qrcode.service.QrImageGenerateService;
import com.myce.qrcode.service.QrStatusService;
import com.myce.reservation.entity.Reserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * QR 코드 생성 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeGenerateServiceImpl implements QrCodeGenerateService {

    private final QrImageGenerateService qrImageGenerateService;
    private final QrStatusService qrStatusService;
    private final S3Service s3Service;

    @Override
    public QrCode createQrCode(Reserver reserver) {
        try {
            // 고유 토큰 생성
            String token = UUID.randomUUID().toString();

            // QR 이미지 생성
            byte[] image = qrImageGenerateService.generateQrImage(token);

            // S3 업로드
            String imageUrl = uploadToStorage(image, token);

            // 상태 및 시간 계산
            LocalDateTime activatedAt = qrStatusService.calculateActivatedAt(reserver);
            LocalDateTime expiredAt = qrStatusService.calculateExpiredAt(reserver);
            QrCodeStatus status = qrStatusService.determineInitialStatus(activatedAt, expiredAt);

            // QR 코드 엔티티 생성
            return QrCode.builder()
                    .reserver(reserver)
                    .qrToken(token)
                    .qrImageUrl(imageUrl)
                    .status(status)
                    .activatedAt(activatedAt)
                    .expiredAt(expiredAt)
                    .build();
        } catch (Exception e) {
            log.error("QR 코드 생성 실패 - 예약자 ID: {}, 오류: {}", reserver.getId(), e.getMessage(), e);
            throw new CustomException(CustomErrorCode.QR_GENERATION_FAILED);
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
}
