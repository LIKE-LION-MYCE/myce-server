package com.myce.system.service.email;

import com.myce.auth.dto.type.LoginType;
import com.myce.system.dto.email.ExpoAdminEmailRequest;

public interface ExpoAdminEmailService {
    void sendMail(Long memberId,
                  LoginType loginType,
                  Long expoId,
                  ExpoAdminEmailRequest dto,
                  String entranceStatus,
                  String name,
                  String phone,
                  String reservationCode,
                  String ticketName);
}
