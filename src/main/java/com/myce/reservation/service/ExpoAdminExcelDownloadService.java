package com.myce.reservation.service;

import com.myce.auth.dto.type.LoginType;

import java.io.OutputStream;

public interface ExpoAdminExcelDownloadService {
    void downloadMyReservationExcelFile(Long expoId, Long memberId, LoginType loginType, OutputStream outputStream);
}
