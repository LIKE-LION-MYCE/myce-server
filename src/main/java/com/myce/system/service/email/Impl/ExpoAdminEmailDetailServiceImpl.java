package com.myce.system.service.email.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.system.dto.email.ExpoAdminEmailDetailResponse;
import com.myce.system.dto.email.ExpoAdminEmailResponse;
import com.myce.system.repository.EmailLogRepository;
import com.myce.system.service.email.ExpoAdminEmailDetailService;
import com.myce.system.service.email.mapper.ExpoAdminEmailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpoAdminEmailDetailServiceImpl implements ExpoAdminEmailDetailService {

    private final ExpoAdminAccessValidate expoAdminAccessValidate;
    private final EmailLogRepository emailLogRepository;
    private final ExpoAdminEmailMapper mapper;

    @Override
    public Page<ExpoAdminEmailResponse> getMyMails(Long expoId,
                                                   Long memberId,
                                                   LoginType loginType,
                                                   String keyword,
                                                   Pageable pageable) {

        expoAdminAccessValidate.ensureViewable(expoId,memberId,loginType, ExpoAdminPermission.EMAIL_LOG_VIEW);

        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if(!hasKeyword){
            return emailLogRepository
                    .findByExpoId(expoId, pageable)
                    .map(mapper::toDto);
        }

        String safeKeyword = java.util.regex.Pattern.quote(keyword.trim());
        return emailLogRepository
                .searchByExpoIdAndKeyword(expoId,safeKeyword,pageable)
                .map(mapper::toDto);
    }

    @Override
    public ExpoAdminEmailDetailResponse getMyMailDetail(Long expoId, Long memberId, LoginType loginType, String emailId) {
        expoAdminAccessValidate.ensureViewable(expoId,memberId,loginType, ExpoAdminPermission.EMAIL_LOG_VIEW);

        return emailLogRepository.findById(emailId)
                .map(mapper::toDetailDto)
                .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_EMAIL_LOG));
    }
}
