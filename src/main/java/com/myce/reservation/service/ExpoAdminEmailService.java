package com.myce.reservation.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.reservation.dto.ExpoAdminEmailRequest;

public interface ExpoAdminEmailService {
    void sendMail(Long memberId, LoginType loginType, Long expoId, ExpoAdminEmailRequest dto);
}
