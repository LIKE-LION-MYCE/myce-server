package com.myce.auth.service;

import com.myce.auth.dto.VerificationEmailRequest;
import com.myce.auth.dto.VerifyEmailCodeRequest;

public interface AuthVerificationService {

    void sendVerificationMail(VerificationEmailRequest request);

    void verifyCode(VerifyEmailCodeRequest request);
}
