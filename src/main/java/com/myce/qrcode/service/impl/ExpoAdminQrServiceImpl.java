package com.myce.qrcode.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.qrcode.dto.ExpoAdminQrReissueRequest;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.qrcode.service.ExpoAdminQrService;
import com.myce.qrcode.service.QrCodeService;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.repository.ReserverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminQrServiceImpl implements ExpoAdminQrService {

    private final ExpoAdminAccessValidate expoAdminAccessValidate;
    private final QrCodeRepository  qrCodeRepository;
    private final ReserverRepository reserverRepository;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public ExpoAdminReservationResponse updateReserverQrCodeForManualCheckIn(Long expoId,
                                                                             Long memberId,
                                                                             LoginType loginType,
                                                                             Long reserverId) {
        expoAdminAccessValidate.ensureEditable(expoId, memberId, loginType, ExpoAdminPermission.RESERVER_LIST_VIEW);

        QrCode qrCode = qrCodeRepository.findByReserverId(reserverId).orElse(null);

        if(qrCode != null){
            if (qrCode.getStatus() == QrCodeStatus.ACTIVE || qrCode.getStatus() == QrCodeStatus.APPROVED) {
                qrCode.markAsUsed();
            }else{
                throw new CustomException(CustomErrorCode.QR_NOT_MANUAL_CHECK_IN);
            }
        }else{
            qrCodeService.issueQr(reserverId);
            QrCode newQrCode = qrCodeRepository.findByReserverId(reserverId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));
            newQrCode.markAsUsed();
        }
        return reserverRepository.findOneResponsesByReserverId(reserverId,expoId);
    }

    @Override
    public List<ExpoAdminReservationResponse> reissueReserverQrCode(Long expoId,
                                                              Long memberId,
                                                              LoginType loginType,
                                                              ExpoAdminQrReissueRequest dto,
                                                              String entranceStatus,
                                                              String name,
                                                              String phone,
                                                              String reservationCode,
                                                              String ticketName) {

        expoAdminAccessValidate.ensureEditable(expoId, memberId, loginType, ExpoAdminPermission.RESERVER_LIST_VIEW);

        if ("입장 만료".equals(entranceStatus) || "티켓 만료".equals(entranceStatus) || "발급 대기".equals(entranceStatus)) {
            throw new CustomException(CustomErrorCode.QR_INVALID_STATUS);
        }

        List<Long> reserverIds = dto.isSelectAllMatching()
                ? reserverRepository.findReserversByFilter(expoId,entranceStatus,name,phone,reservationCode,ticketName)
                .stream().map(Reserver::getId).toList()
                : dto.getReserverIds();

        for (Long reserverId : reserverIds){
            QrCode existingQrCode = qrCodeRepository.findByReserverId(reserverId)
                            .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

            switch (existingQrCode.getStatus()) {
                case USED, EXPIRED -> {
                    throw new CustomException(CustomErrorCode.QR_INVALID_STATUS);
                }
                case ACTIVE, APPROVED -> {
                    qrCodeService.reissueQr(reserverId, memberId, loginType);
                }
            }
        }

        return reserverRepository.findResponsesByReserverIds(reserverIds,expoId);
    }
}
