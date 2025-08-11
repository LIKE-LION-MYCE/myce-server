package com.myce.qrcode.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.reservation.dto.ExpoAdminReservationResponse;

public interface ExpoAdminManualCheckInService {
    ExpoAdminReservationResponse updateReserverQrCodeForManualCheckIn(Long expoId,
                                                                      Long memberId,
                                                                      LoginType loginType,
                                                                      Long reserverId);
}
