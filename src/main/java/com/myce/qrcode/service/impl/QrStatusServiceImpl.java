package com.myce.qrcode.service.impl;

import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.qrcode.service.QrStatusService;
import com.myce.reservation.entity.Reserver;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * QR 코드 상태 관리 서비스 구현체
 */
@Service
public class QrStatusServiceImpl implements QrStatusService {

    @Override
    public LocalDateTime calculateActivatedAt(Reserver reserver) {
        LocalDate ticketUseStartDate = reserver.getReservation().getTicket().getUseStartDate();
        return ticketUseStartDate.atStartOfDay();
    }

    @Override
    public LocalDateTime calculateExpiredAt(Reserver reserver) {
        LocalDate ticketUseEndDate = reserver.getReservation().getTicket().getUseEndDate();
        return ticketUseEndDate.plusDays(1).atStartOfDay();
    }

    @Override
    public QrCodeStatus determineInitialStatus(LocalDateTime activatedAt, LocalDateTime expiredAt) {
        LocalDateTime now = LocalDateTime.now();
        return (now.isAfter(activatedAt) || now.isEqual(activatedAt)) && now.isBefore(expiredAt)
                ? QrCodeStatus.ACTIVE
                : QrCodeStatus.APPROVED;
    }
}
