package com.myce.auth.repository;

import com.myce.auth.entity.EmailVerificationInfo;

public interface EmailVerificationRepository {

    void save(EmailVerificationInfo verificationInfo, int limitTime);

    EmailVerificationInfo findByEmail(String email);

}
