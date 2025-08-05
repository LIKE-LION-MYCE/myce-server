package com.myce.qrcode.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final ReserverRepository reserverRepository;

    private byte[] generateQrImage(String text) throws Exception {
        BitMatrix matrix = new MultiFormatWriter()
                .encode(text, BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    @Override
    @Transactional
    public QrCode issueQr(Long reserverId) throws Exception {
        log.info("QR 코드 발급 시작 - 예약자 ID: {}", reserverId);
        
        try {
            Reserver reserver = reserverRepository.findById(reserverId)
                    .orElseThrow(() -> {
                        log.error("예약자를 찾을 수 없음 - ID: {}", reserverId);
                        return new IllegalArgumentException("예약자를 찾을 수 없습니다.");
                    });

            if (qrCodeRepository.findByReserver(reserver).isPresent()) {
                log.warn("이미 QR 코드가 발급된 예약자 - ID: {}", reserverId);
                throw new IllegalStateException("이미 QR 코드가 발급된 예약자입니다.");
            }

            String token = UUID.randomUUID().toString();
            byte[] image = generateQrImage(token);
            String imageUrl = uploadToStorage(image, token);

            QrCode qr = QrCode.builder()
                    .reserver(reserver)
                    .qrToken(token)
                    .qrImageUrl(imageUrl)
                    .status(QrCodeStatus.ACTIVE)
                    .build();

            QrCode savedQr = qrCodeRepository.save(qr);
            log.info("QR 코드 발급 완료 - 예약자 ID: {}, QR ID: {}", reserverId, savedQr.getId());
            
            return savedQr;
        } catch (Exception e) {
            log.error("QR 코드 발급 실패 - 예약자 ID: {}, 오류: {}", reserverId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public QrCode reissueQr(Long reserverId, Long adminMemberId) throws Exception {
        log.info("QR 코드 재발급 시작 - 예약자 ID: {}, 관리자 ID: {}", reserverId, adminMemberId);
        
        try {
            Reserver reserver = reserverRepository.findById(reserverId)
                    .orElseThrow(() -> {
                        log.error("예약자를 찾을 수 없음 - ID: {}", reserverId);
                        return new IllegalArgumentException("예약자를 찾을 수 없습니다.");
                    });

            validateExpoManager(adminMemberId, reserver);
            log.info("관리자 권한 검증 완료 - 관리자 ID: {}, 예약자 ID: {}", adminMemberId, reserverId);

            QrCode existing = qrCodeRepository.findByReserver(reserver)
                    .orElseThrow(() -> {
                        log.error("기존 QR 코드를 찾을 수 없음 - 예약자 ID: {}", reserverId);
                        return new IllegalStateException("기존 QR 코드가 존재하지 않습니다.");
                    });

            log.info("기존 QR 코드 만료 처리 - QR ID: {}", existing.getId());
            existing.expire();
            qrCodeRepository.save(existing);

            QrCode newQr = issueQr(reserverId);
            log.info("QR 코드 재발급 완료 - 예약자 ID: {}, 새 QR ID: {}", reserverId, newQr.getId());
            
            return newQr;
        } catch (Exception e) {
            log.error("QR 코드 재발급 실패 - 예약자 ID: {}, 관리자 ID: {}, 오류: {}", 
                     reserverId, adminMemberId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void markQrAsUsed(String qrToken, Long adminMemberId) {
        log.info("QR 코드 사용 처리 시작 - 토큰: {}, 관리자 ID: {}", qrToken, adminMemberId);
        
        try {
            QrCode qr = qrCodeRepository.findByQrToken(qrToken)
                    .orElseThrow(() -> {
                        log.error("유효하지 않은 QR 토큰 - 토큰: {}", qrToken);
                        return new IllegalArgumentException("유효하지 않은 QR 토큰입니다.");
                    });

            validateExpoManager(adminMemberId, qr.getReserver());
            log.info("관리자 권한 검증 완료 - 관리자 ID: {}, QR ID: {}", adminMemberId, qr.getId());

            if (qr.getStatus() != QrCodeStatus.ACTIVE) {
                log.warn("사용할 수 없는 QR 코드 상태 - QR ID: {}, 상태: {}", qr.getId(), qr.getStatus());
                throw new IllegalStateException("이미 사용되었거나 만료된 QR 코드입니다.");
            }

            qr.markAsUsed();
            qrCodeRepository.save(qr);
            log.info("QR 코드 사용 처리 완료 - QR ID: {}, 예약자 ID: {}", 
                    qr.getId(), qr.getReserver().getId());
                    
        } catch (Exception e) {
            log.error("QR 코드 사용 처리 실패 - 토큰: {}, 관리자 ID: {}, 오류: {}", 
                     qrToken, adminMemberId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QrCode> getQrByReserverId(Long reserverId) {
        log.info("예약자 ID로 QR 코드 조회 - 예약자 ID: {}", reserverId);
        
        try {
            Optional<Reserver> reserver = reserverRepository.findById(reserverId);
            if (reserver.isEmpty()) {
                log.warn("존재하지 않는 예약자 ID - ID: {}", reserverId);
                return Optional.empty();
            }
            
            Optional<QrCode> qrCode = qrCodeRepository.findByReserver(reserver.get());
            log.info("QR 코드 조회 결과 - 예약자 ID: {}, 존재 여부: {}", reserverId, qrCode.isPresent());
            
            return qrCode;
        } catch (Exception e) {
            log.error("QR 코드 조회 실패 - 예약자 ID: {}, 오류: {}", reserverId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QrCode> getQrByToken(String token) {
        log.info("토큰으로 QR 코드 조회 - 토큰: {}", token);
        
        try {
            Optional<QrCode> qrCode = qrCodeRepository.findByQrToken(token);
            log.info("QR 코드 조회 결과 - 토큰: {}, 존재 여부: {}", token, qrCode.isPresent());
            
            return qrCode;
        } catch (Exception e) {
            log.error("QR 코드 조회 실패 - 토큰: {}, 오류: {}", token, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private void validateExpoManager(Long adminId, Reserver reserver) {
        Long managerId = reserver.getReservation()
                .getExpo()
                .getMember()
                .getId();

        if (!managerId.equals(adminId)) {
            throw new SecurityException("해당 박람회의 관리자만 처리할 수 있습니다.");
        }
    }

    private String uploadToStorage(byte[] image, String token) throws IOException {

        String directoryPath = "src/main/resources/static/qr";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = directoryPath + "/" + token + ".png";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(image);
        }

        return "/qr/" + token + ".png";
    }

}
