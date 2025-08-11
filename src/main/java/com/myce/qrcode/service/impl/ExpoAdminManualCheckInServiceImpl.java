package com.myce.qrcode.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.qrcode.service.ExpoAdminManualCheckInService;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import com.myce.reservation.repository.ReserverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpoAdminManualCheckInServiceImpl implements ExpoAdminManualCheckInService {

    private final ExpoRepository expoRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final QrCodeRepository  qrCodeRepository;
    private final ReserverRepository reserverRepository;

    @Override
    @Transactional
    public ExpoAdminReservationResponse updateReserverQrCodeForManualCheckIn(Long expoId,
                                                                             Long memberId,
                                                                             LoginType loginType,
                                                                             Long reserverId) {
        validateMyAccess(expoId, memberId, loginType);

        QrCode qrCode = qrCodeRepository.findByReserverId(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_FOUND));

        qrCode.markAsUsed();

        return reserverRepository.findOneResponsesByReserverId(reserverId);
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
