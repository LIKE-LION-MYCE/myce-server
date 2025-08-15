package com.myce.qrcode.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.qrcode.dto.ExpoAdminQrReissueRequest;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface ExpoAdminQrService {
    ExpoAdminReservationResponse updateReserverQrCodeForManualCheckIn(Long expoId,
                                                                      Long memberId,
                                                                      LoginType loginType,
                                                                      Long reserverId);

    List<ExpoAdminReservationResponse> reissueReserverQrCode(Long expoId,
                                                            Long memberId,
                                                            LoginType loginType,
                                                            @Valid ExpoAdminQrReissueRequest dto,
                                                            String entranceStatus,
                                                            String name,
                                                            String phone,
                                                            String reservationCode,
                                                            String ticketName);
}
