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

        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new RuntimeException("예약자 없음"));

        if (qrCodeRepository.findByReserver(reserver).isPresent()) {
            throw new IllegalStateException("이미 QR이 발급된 예약자입니다.");
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

        return qrCodeRepository.save(qr);
    }

    @Override
    @Transactional
    public QrCode reissueQr(Long reserverId, Long adminMemberId) throws Exception {
        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new RuntimeException("예약자 없음"));

        validateExpoManager(adminMemberId, reserver);

        QrCode existing = qrCodeRepository.findByReserver(reserver)
                .orElseThrow(() -> new RuntimeException("기존 QR 없음"));

        existing.expire();
        qrCodeRepository.save(existing);

        return issueQr(reserverId);
    }

    @Override
    @Transactional
    public void markQrAsUsed(String qrToken, Long adminMemberId) {
        QrCode qr = qrCodeRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new RuntimeException("QR 토큰 유효하지 않음"));

        validateExpoManager(adminMemberId, qr.getReserver());

        if (qr.getStatus() != QrCodeStatus.ACTIVE) {
            throw new IllegalStateException("이미 사용되었거나 만료된 QR입니다.");
        }

        qr.markAsUsed();
        qrCodeRepository.save(qr);
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
