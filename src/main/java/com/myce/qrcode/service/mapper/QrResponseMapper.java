package com.myce.qrcode.service.mapper;

import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
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
        
        switch (status) {
            case APPROVED:
                return QrVerifyResponse.invalid("QR 코드가 활성화되지 않았습니다.", "APPROVED");
                
            case USED:
                return QrVerifyResponse.invalid("이미 사용된 QR 코드입니다.", "USED");
                
            case EXPIRED:
                return QrVerifyResponse.invalid("만료된 QR 코드입니다.", "EXPIRED");
                
            case ACTIVE:
                // 유효한 QR 코드 - 예약자 정보와 함께 반환
                Reserver reserver = qrCode.getReserver();
                return QrVerifyResponse.valid(
                        reserver.getName(),
                        reserver.getReservation().getExpo().getTitle(),
                        reserver.getReservation().getTicket().getName()
                );
                
            default:
                return QrVerifyResponse.invalid("알 수 없는 QR 코드 상태입니다.", status.name());
        }
    }
}