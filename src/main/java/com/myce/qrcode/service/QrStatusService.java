package com.myce.qrcode.service;

import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;

import java.time.LocalDateTime;

/**
 * QR 코드 상태 관리 서비스
 */
public interface QrStatusService {

    LocalDateTime calculateActivatedAt(Reserver reserver);

    LocalDateTime calculateExpiredAt(Reserver reserver);

    QrCodeStatus determineInitialStatus(LocalDateTime activatedAt, LocalDateTime expiredAt);
}
