package com.myce.auth.repository;

import com.myce.auth.entity.EmailVerificationInfo;

public interface EmailVerificationRepository {

    void save(EmailVerificationInfo verificationInfo, String type, int limitTime);

    EmailVerificationInfo findByEmail(String type, String email);

}
