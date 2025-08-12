package com.myce.qrcode.service.mapper;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class QrResponseMapper {

    /**
     * QR 사용 성공 응답 생성
     */
    public QrUseResponse toUseSuccessResponse(QrCode qrCode) {
        Reserver reserver = qrCode.getReserver();
        return QrUseResponse.success(
                reserver.getName(),
                reserver.getReservation().getExpo().getTitle(),
                reserver.getReservation().getTicket().getName()
        );
    }

    /**
     * QR 검증 응답 생성 (상태만 체크)
     */
    public QrVerifyResponse toVerifyResponse(QrCode qrCode) {

        QrCodeStatus status = qrCode.getStatus();

        return switch (status) {
            case APPROVED, USED, EXPIRED -> QrVerifyResponse.invalid( status.getMessage(), status.name());
            case ACTIVE -> {
                // 유효한 QR 코드 - 예약자 정보와 함께 반환
                Reserver reserver = qrCode.getReserver();
                yield QrVerifyResponse.valid(status.getMessage(), reserver.getName(),
                        reserver.getReservation().getExpo().getTitle(),
                        reserver.getReservation().getTicket().getName(), status.name());
            }
        };
    }

}