package com.myce.system.service.email;

import com.myce.auth.dto.type.LoginType;
import com.myce.system.dto.email.ExpoAdminEmailRequest;
import com.myce.system.dto.email.ExpoAdminEmailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpoAdminEmailService {
    void sendMail(Long memberId, LoginType loginType, Long expoId, ExpoAdminEmailRequest dto);
    Page<ExpoAdminEmailResponse> getMyMails(Long expoId,
                                            Long memberId,
                                            LoginType loginType,
                                            String subject,
                                            String content,
                                            Pageable pageable);
}
