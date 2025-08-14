package com.myce.system.service.email;

import com.myce.auth.dto.type.LoginType;
import com.myce.system.dto.email.ExpoAdminEmailDetailResponse;
import com.myce.system.dto.email.ExpoAdminEmailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpoAdminEmailDetailService {
    Page<ExpoAdminEmailResponse> getMyMails(Long expoId,
                                            Long memberId,
                                            LoginType loginType,
                                            String keyword,
                                            Pageable pageable);

    ExpoAdminEmailDetailResponse getMyMailDetail(Long expoId, Long memberId, LoginType loginType, String emailId);
}