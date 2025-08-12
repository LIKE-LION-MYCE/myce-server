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
    public QrUseResponse toUseResponse(QrCode qrCode) {

        QrCodeStatus status = qrCode.getStatus();

        return switch (status) {
            case ACTIVE -> new QrUseResponse(true, SUCCESS_MESSAGE);
            case USED, EXPIRED, APPROVED -> new QrUseResponse(false, status.getMessage());
        };
    }


    /**
     * QR 검증 응답 생성 (상태만  체크)
     */
    public QrVerifyResponse toVerifyResponse(QrCode qrCode) {

        QrCodeStatus status = qrCode.getStatus();

        return switch (status) {
            case APPROVED, USED, EXPIRED -> new QrVerifyResponse(status.getMessage(), status.name());
            case ACTIVE -> {
                // 유효한 QR 코드 - 예약자 정보와 함께 반환
                Reserver reserver = qrCode.getReserver();
                yield   new QrVerifyResponse(status.getMessage(), reserver.getName(),
                        reserver.getReservation().getExpo().getTitle(),
                        reserver.getReservation().getTicket().getName(), status.name());
            }
        };
    }

}