package com.myce.qrcode.service.mapper;

import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QrResponseMapper {

    private static final String SUCCESS_MESSAGE = "QR 코드가 성공적으로 사용처리 되었습니다";

    /**
     * QR 사용 응답 생성 (성공/실패 모두 처리)
     */
    public QrUseResponse toUseResponse(QrCode qrCode, boolean wasSuccessfullyUsed) {

        if (wasSuccessfullyUsed) {
            // 성공적으로 사용 처리된 경우
            Reserver reserver = qrCode.getReserver();
            return QrUseResponse.success(SUCCESS_MESSAGE, reserver.getReservation().getTicket().getName());
        } else {
            // 사용 처리되지 않은 경우 (APPROVED, EXPIRED, USED 등)
            QrCodeStatus status = qrCode.getStatus();
            return QrUseResponse.fail(status.getMessage());
        }
    }

    /**
     * QR 사용 응답 생성 (기존 호환성 유지)
     */
    public QrUseResponse toUseResponse(QrCode qrCode) {
        QrCodeStatus status = qrCode.getStatus();
        return switch (status) {
            case ACTIVE -> {
                Reserver reserver = qrCode.getReserver();
                yield QrUseResponse.success(SUCCESS_MESSAGE, reserver.getReservation().getTicket().getName());
            }
            case USED, EXPIRED, APPROVED -> QrUseResponse.fail(status.getMessage());
        };
    }


    /**
     * QR 검증 응답 생성 (상태만  체크)
     */
    public QrVerifyResponse toVerifyResponse(QrCode qrCode) {

        QrCodeStatus status = qrCode.getStatus();

        return switch (status) {
            case APPROVED, USED, EXPIRED -> QrVerifyResponse.fail(status.getMessage(), status.name());
            case ACTIVE -> {
                // 유효한 QR 코드 - 예약자 정보와 함께 반환
                Reserver reserver = qrCode.getReserver();
                yield   QrVerifyResponse.success(status.getMessage(), reserver.getName(),
                        reserver.getReservation().getExpo().getTitle(),
                        reserver.getReservation().getTicket().getName(), status.name());
            }
        };
    }

}