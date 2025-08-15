package com.myce.qrcode.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpoAdminQrServiceImpl implements ExpoAdminQrService {

    private final ExpoRepository expoRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final QrCodeRepository  qrCodeRepository;
    private final ReserverRepository reserverRepository;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public ExpoAdminReservationResponse updateReserverQrCodeForManualCheckIn(Long expoId,
                                                                             Long memberId,
                                                                             LoginType loginType,
                                                                             Long reserverId) {
        validateMyAccess(expoId, memberId, loginType);

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

        validateMyAccess(expoId, memberId, loginType);

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

    private void validateMyAccess(Long expoId, Long memberId, LoginType loginType) {
        if(memberId == null || loginType == null){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch(loginType){
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                if(!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsReserverListViewTrue(memberId, expoId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}
